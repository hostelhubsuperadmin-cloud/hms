import axiosClient from "../api/axiosClient";

const publicHostelService = {
  async getAll(city = "") {
    const { data } = await axiosClient.get("/api/public/hostels", {
      params: city.trim() ? { city: city.trim() } : {},
    });
    return data;
  },

  async getById(id) {
    const { data } = await axiosClient.get(
      `/api/public/hostels/${id}`
    );
    return data;
  },

  async getAvailability(id) {
    const { data } = await axiosClient.get(
      `/api/public/hostels/${id}/availability`
    );
    return data;
  },
  
async getMenu(id) {
  const { data } = await axiosClient.get(
    `/api/public/hostels/${id}/menu`
  );
  return data;
},

};

export default publicHostelService;