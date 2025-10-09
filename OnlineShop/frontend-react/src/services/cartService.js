import api from "../api/axios";

const cartService = {
  getCart: async (customerId) => {
    const { data } = await api.get(`/cart/${customerId}`);
    return data;
  },
  addItem: async (customerId, payload) => {
    const { data } = await api.post(`/cart/${customerId}/items`, payload);
    return data;
  },
  updateItem: async (customerId, productId, quantity) => {
    const { data } = await api.put(`/cart/${customerId}/items/${productId}`, null, {
      params: { quantity }
    });
    return data;
  },
  removeItem: async (customerId, productId) => {
    const { data } = await api.delete(`/cart/${customerId}/items/${productId}`);
    return data;
  }
};

export default cartService;
