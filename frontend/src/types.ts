export interface AuthRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tokenType?: string; // Burayı ekle
}

export interface FileItem {
  id: number; // backend'deki File Entity'de id Long olduğu için number
  name: string;
  filePath: string; // dosya indirme için gerekli
  type?: string;
  size?: number;
}
