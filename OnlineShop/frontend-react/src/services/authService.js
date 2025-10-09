import api from "../api/axios";

const authService = {
  login: async (payload) => {
    const { data } = await api.post("/auth/login", payload);
    return data;
  },
  register: async (payload) => {
    const { data } = await api.post("/auth/register", payload);
    return data;
  },
  refresh: async (refreshToken) => {
    const { data } = await api.post("/auth/refresh", { refreshToken });
    return data;
  },
  fetchProfile: async () => {
    try {
      const { data } = await api.get("/users/me");
      return data;
    } catch (error) {
      if (error.response?.status === 404) {
        return {};
      }
      throw error;
    }
  }
};

export default authService;
