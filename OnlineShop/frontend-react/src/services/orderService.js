import api from "../api/axios";

const orderService = {
  getAll: async () => {
    const { data } = await api.get("/orders");
    return data;
  },
  getForCustomer: async (customerId) => {
    const { data } = await api.get(`/orders/customer/${customerId}`);
    return data;
  },
  updateStatus: async (orderId, status) => {
    const { data } = await api.patch(`/orders/${orderId}/status`, null, {
      params: { status }
    });
    return data;
  },
  create: async (payload) => {
    const { data } = await api.post("/orders", payload);
    return data;
  }
};

export default orderService;
