import api from "../api/axios";

const productService = {
  getAll: async () => {
    const { data } = await api.get("/products");
    return data;
  },
  create: async (payload) => {
    const { data } = await api.post("/products", payload);
    return data;
  },
  update: async (productId, payload) => {
    const { data } = await api.put(`/products/${productId}`, payload);
    return data;
  },
  remove: async (productId) => {
    await api.delete(`/products/${productId}`);
  }
};

export default productService;
