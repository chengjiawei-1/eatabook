import axios from "axios";

const API_BASE_URL = '/api/users';
const GRAPH_API_BASE_URL = '/api/graph';

interface LoginResponse {
  code: number;
  msg: string;
  data?: {
    id?: number;
    username: string;
    email?: string;
    password?: string;
    [key: string]: any;
  };
}

interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

// 图谱数据接口定义
interface GraphNode {
  id: string;
  name: string;
  type: string;
  [key: string]: any;
}

interface GraphLink {
  id?: string;
  source: string | GraphNode;
  target: string | GraphNode;
  value?: number;
  [key: string]: any;
}

interface GraphData {
  nodes: GraphNode[];
  links: GraphLink[];
  [key: string]: any;
}

export const authAPI = {
  // 登录
  login: async (username: string, password: string): Promise<LoginResponse> => {
    const response = await fetch(`${API_BASE_URL}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username,
        password,
      }),
    });

    if (!response.ok) {
      throw new Error('网络请求失败');
    }

    return response.json();
  },

  // 注册
  register: async (data: RegisterRequest): Promise<string> => {
    const response = await fetch(`${API_BASE_URL}/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error('网络请求失败');
    }

    const text = await response.text();
    return text;
  },
};

// 图谱相关 API
export const graphAPI = {
  // 获取图谱数据
  getGraphData: async (): Promise<GraphData> => {
    const response = await axios.get(GRAPH_API_BASE_URL);
    return response.data;
  },
};
