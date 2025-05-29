import React, { useEffect, useState } from 'react';
import api from './api';
import { FileItem } from './types';

type Message = { text: string; type: 'error' | 'info' } | null;

const styles = {
  pageWrapper: {
    minHeight: '100vh',
    backgroundColor: '#e0f7fa',
    padding: 40,
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'flex-start',
    fontFamily: 'Arial, sans-serif',
  },
  container: {
    width: '100%',
    maxWidth: 600,
    backgroundColor: '#fff',
    borderRadius: 8,
    boxShadow: '0 8px 20px rgba(0,0,0,0.15)',
    padding: 20,
  },
  title: {
    textAlign: 'center' as const,
    fontSize: 24,
    fontWeight: 'bold' as const,
    color: '#333',
    marginBottom: 20,
  },
  message: {
    backgroundColor: '#007BFF',
    color: 'white',
    padding: 10,
    borderRadius: 4,
    marginBottom: 20,
    textAlign: 'center' as const,
  },
  fileInputLabel: {
    display: 'inline-block',
    backgroundColor: '#007BFF',
    color: 'white',
    padding: '8px 16px',
    borderRadius: 4,
    cursor: 'pointer',
    marginRight: 10,
  },
  fileName: {
    display: 'inline-block',
    maxWidth: 250,
    whiteSpace: 'nowrap' as const,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    verticalAlign: 'middle',
    marginRight: 10,
  },
  button: {
    padding: '8px 16px',
    borderRadius: 4,
    border: 'none',
    cursor: 'pointer',
    color: 'white',
    fontWeight: 'bold' as const,
  },
  uploadButton: {
    backgroundColor: '#28a745',
  },
  uploadButtonDisabled: {
    backgroundColor: '#6c757d',
    cursor: 'not-allowed',
  },
  hr: {
    margin: '30px 0',
    borderColor: '#ddd',
  },
  fileList: {
    listStyle: 'none',
    padding: 0,
  },
  fileListItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 12,
    border: '1px solid #ddd',
    borderRadius: 4,
    marginBottom: 10,
  },
  fileNameItem: {
    maxWidth: 300,
    whiteSpace: 'nowrap' as const,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  actionButton: {
    marginLeft: 8,
    padding: '6px 12px',
    borderRadius: 4,
    border: 'none',
    cursor: 'pointer',
    color: 'white',
    fontWeight: 'bold' as const,
  },
  downloadButton: {
    backgroundColor: '#007BFF',
  },
  deleteButton: {
    backgroundColor: '#dc3545',
  },
  noFiles: {
    textAlign: 'center' as const,
    color: '#666',
  },
  tokenInputContainer: {
    marginTop: 20,
    padding: 15,
    backgroundColor: '#f8f9fa',
    borderRadius: 4,
    border: '1px solid #ddd',
  },
  tokenInput: {
    width: '100%',
    padding: 8,
    marginBottom: 10,
    borderRadius: 4,
    border: '1px solid #ced4da',
  },
  tokenButton: {
    backgroundColor: '#17a2b8',
    color: 'white',
    padding: '8px 16px',
    borderRadius: 4,
    border: 'none',
    cursor: 'pointer',
    width: '100%',
  },
  tokenTitle: {
    marginTop: 0,
    marginBottom: 10,
    color: '#333',
  },
  tokenSection: {
    marginBottom: 20,
  },
};

const FileManager: React.FC = () => {
  const [files, setFiles] = useState<FileItem[]>([]);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [message, setMessage] = useState<Message>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [shareToken, setShareToken] = useState<string | null>(null);
  const [tokenInput, setTokenInput] = useState('');

  useEffect(() => {
    fetchFiles();
  }, []);

  const fetchFiles = async () => {
    try {
      const response = await api.get<FileItem[]>('/files/list');
      setFiles(response.data);
    } catch {
      setMessage({ text: 'Unable to load files.', type: 'error' });
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setIsLoading(true);
    setMessage(null);
    try {
      const formData = new FormData();
      formData.append('file', selectedFile);

      await api.post('/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setSelectedFile(null);
      fetchFiles();
      setMessage({ text: 'File uploaded successfully!', type: 'info' });
    } catch {
      setMessage({ text: 'Failed to upload the file.', type: 'error' });
    } finally {
      setIsLoading(false);
    }
  };

  const downloadFile = async (file: FileItem) => {
    setMessage(null);
    try {
      const response = await api.get(`/files/download/${file.filePath}`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(response.data as Blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', file.name);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      setMessage({ text: 'Failed to download the file.', type: 'error' });
    }
  };

  const deleteFile = async (file: FileItem) => {
    setMessage(null);
    try {
      await api.delete(`/files/delete/${file.id}`);
      fetchFiles();
      setMessage({ text: 'File deleted successfully!', type: 'info' });
    } catch {
      setMessage({ text: 'Failed to delete the file.', type: 'error' });
    }
  };

  const shareFile = async (file: FileItem) => {
    setMessage(null);
    setShareToken(null);
    try {
      const response = await api.post<{ token: string }>('/share/shared-files/create', {
        fileId: file.id,
        expiresInDays: 1,
      });
      setShareToken(response.data.token);
      setMessage({ text: 'Share token created successfully!', type: 'info' });
    } catch {
      setMessage({ text: 'Failed to create share token.', type: 'error' });
    }
  };

  const downloadWithToken = async () => {
    if (!tokenInput.trim()) {
      setMessage({ text: 'Please enter a share token', type: 'error' });
      return;
    }
    
    setMessage(null);
    try {
      const response = await api.get(`/share/download/shared/${tokenInput}`, {
        responseType: 'blob',
      });
      
      const contentDisposition = response.headers['content-disposition'];
      let filename = 'downloaded_file';
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename="?([^"]+)"?/);
        if (filenameMatch && filenameMatch[1]) {
          filename = filenameMatch[1];
        }
      }
      
      const url = window.URL.createObjectURL(response.data as Blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
      setMessage({ text: 'File downloaded successfully using token!', type: 'info' });
    } catch {
      setMessage({ text: 'Invalid token or file not found.', type: 'error' });
    }
  };

  return (
    <div style={styles.pageWrapper}>
      <div style={styles.container}>
        <h2 style={styles.title}>Secure File Manager</h2>

        {message && (
          <p style={{ 
            ...styles.message, 
            backgroundColor: message.type === 'error' ? '#dc3545' : '#007BFF' 
          }}>
            {message.text}
          </p>
        )}

        <div style={styles.tokenSection}>
          <div style={{ marginBottom: 20 }}>
            <label style={styles.fileInputLabel}>
              Select File
              <input
                type="file"
                style={{ display: 'none' }}
                onChange={e => setSelectedFile(e.target.files?.[0] ?? null)}
              />
            </label>

            {selectedFile && <span style={styles.fileName}>{selectedFile.name}</span>}

            {selectedFile && (
              <button
                onClick={handleUpload}
                disabled={isLoading}
                style={{
                  ...styles.button,
                  ...(isLoading ? styles.uploadButtonDisabled : styles.uploadButton),
                }}
              >
                {isLoading ? 'Uploading...' : 'Upload'}
              </button>
            )}
          </div>
        </div>

        <div style={styles.tokenInputContainer}>
          <h3 style={styles.tokenTitle}>Download with Share Token</h3>
          <input
            type="text"
            placeholder="Enter share token here"
            value={tokenInput}
            onChange={(e) => setTokenInput(e.target.value)}
            style={styles.tokenInput}
          />
          <button
            onClick={downloadWithToken}
            style={styles.tokenButton}
          >
            Download Shared File
          </button>
        </div>

        <hr style={styles.hr} />

        <h3>Your Files</h3>
        {files.length === 0 ? (
          <p style={styles.noFiles}>No files have been uploaded yet.</p>
        ) : (
          <ul style={styles.fileList}>
            {files.map(file => (
              <li key={file.id} style={styles.fileListItem}>
                <span style={styles.fileNameItem}>{file.name}</span>

                <div>
                  <button
                    onClick={() => downloadFile(file)}
                    style={{ ...styles.actionButton, ...styles.downloadButton }}
                  >
                    Download
                  </button>
                  <button
                    onClick={() => deleteFile(file)}
                    style={{ ...styles.actionButton, ...styles.deleteButton }}
                  >
                    Delete
                  </button>
                  <button
                    onClick={() => shareFile(file)}
                    style={{ ...styles.actionButton, backgroundColor: '#17a2b8' }}
                  >
                    Share
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}

        {shareToken && (
          <div style={{ marginTop: 20, padding: 10, backgroundColor: '#d1ecf1', borderRadius: 4 }}>
            <strong>Share Token Created:</strong> {shareToken}
            <br />
            <small>This token will expire in 1 day. Share it with others to allow file access.</small>
          </div>
        )}
      </div>
    </div>
  );
};

export default FileManager;