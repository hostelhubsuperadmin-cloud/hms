
import axiosClient from "../api/axiosClient";

const hostelAdminService = {
  // Assigned hostels
  getMyHostels: async () => {
    const { data } = await axiosClient.get("/api/hostel-admin/my-hostels");
    return data;
  },

  // Blocks
  getBlocks: async (hostelId) => {
    const { data } = await axiosClient.get(
      `/api/hostels/${hostelId}/blocks`
    );
    return data;
  },

  createBlock: async (hostelId, payload) => {
    const { data } = await axiosClient.post(
      `/api/hostels/${hostelId}/blocks`,
      payload
    );
    return data;
  },

  updateBlock: async (hostelId, blockId, payload) => {
    const { data } = await axiosClient.put(
      `/api/hostels/${hostelId}/blocks/${blockId}`,
      payload
    );
    return data;
  },

  changeBlockStatus: async (hostelId, blockId, status) => {
    const { data } = await axiosClient.patch(
      `/api/hostels/${hostelId}/blocks/${blockId}/status`,
      null,
      { params: { status } }
    );
    return data;
  },

  // Floors
  getFloors: async (hostelId, blockId) => {
    const { data } = await axiosClient.get(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors`
    );
    return data;
  },

  createFloor: async (hostelId, blockId, payload) => {
    const { data } = await axiosClient.post(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors`,
      payload
    );
    return data;
  },

  updateFloor: async (hostelId, blockId, floorId, payload) => {
    const { data } = await axiosClient.put(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}`,
      payload
    );
    return data;
  },

  changeFloorStatus: async (hostelId, blockId, floorId, status) => {
    const { data } = await axiosClient.patch(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/status`,
      null,
      { params: { status } }
    );
    return data;
  },

  // Flats
  getFlats: async (hostelId, blockId, floorId) => {
    const { data } = await axiosClient.get(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats`
    );
    return data;
  },

  createFlat: async (hostelId, blockId, floorId, payload) => {
    const { data } = await axiosClient.post(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats`,
      payload
    );
    return data;
  },

  updateFlat: async (hostelId, blockId, floorId, flatId, payload) => {
    const { data } = await axiosClient.put(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}`,
      payload
    );
    return data;
  },

  changeFlatStatus: async (hostelId, blockId, floorId, flatId, status) => {
    const { data } = await axiosClient.patch(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/status`,
      null,
      { params: { status } }
    );
    return data;
  },

  // Rooms directly on a floor
  getRooms: async (hostelId, blockId, floorId) => {
    const { data } = await axiosClient.get(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms`
    );
    return data;
  },

  createRoom: async (hostelId, blockId, floorId, payload) => {
    const { data } = await axiosClient.post(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms`,
      payload
    );
    return data;
  },

  updateRoom: async (hostelId, blockId, floorId, roomId, payload) => {
    const { data } = await axiosClient.put(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms/${roomId}`,
      payload
    );
    return data;
  },

  changeRoomStatus: async (hostelId, blockId, floorId, roomId, status) => {
    const { data } = await axiosClient.patch(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms/${roomId}/status`,
      null,
      { params: { status } }
    );
    return data;
  },

  // Rooms inside a flat
  getFlatRooms: async (hostelId, blockId, floorId, flatId) => {
    const { data } = await axiosClient.get(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms`
    );
    return data;
  },

  createFlatRoom: async (hostelId, blockId, floorId, flatId, payload) => {
    const { data } = await axiosClient.post(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms`,
      payload
    );
    return data;
  },

  updateFlatRoom: async (
    hostelId, blockId, floorId, flatId, roomId, payload
  ) => {
    const { data } = await axiosClient.put(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms/${roomId}`,
      payload
    );
    return data;
  },

  changeFlatRoomStatus: async (
    hostelId, blockId, floorId, flatId, roomId, status
  ) => {
    const { data } = await axiosClient.patch(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms/${roomId}/status`,
      null,
      { params: { status } }
    );
    return data;
  },

  // Beds in direct-floor rooms
  getBeds: async (hostelId, blockId, floorId, roomId) => {
    const { data } = await axiosClient.get(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms/${roomId}/beds`
    );
    return data;
  },

  createBed: async (hostelId, blockId, floorId, roomId, payload) => {
    const { data } = await axiosClient.post(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms/${roomId}/beds`,
      payload
    );
    return data;
  },

  updateBed: async (
    hostelId, blockId, floorId, roomId, bedId, payload
  ) => {
    const { data } = await axiosClient.put(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms/${roomId}/beds/${bedId}`,
      payload
    );
    return data;
  },

  changeBedStatus: async (
    hostelId, blockId, floorId, roomId, bedId, status
  ) => {
    const { data } = await axiosClient.patch(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms/${roomId}/beds/${bedId}/status`,
      null,
      { params: { status } }
    );
    return data;
  },

  // Beds in rooms inside a flat
  getFlatRoomBeds: async (
    hostelId, blockId, floorId, flatId, roomId
  ) => {
    const { data } = await axiosClient.get(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms/${roomId}/beds`
    );
    return data;
  },

  createFlatRoomBed: async (
    hostelId, blockId, floorId, flatId, roomId, payload
  ) => {
    const { data } = await axiosClient.post(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms/${roomId}/beds`,
      payload
    );
    return data;
  },

  updateFlatRoomBed: async (
    hostelId, blockId, floorId, flatId, roomId, bedId, payload
  ) => {
    const { data } = await axiosClient.put(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms/${roomId}/beds/${bedId}`,
      payload
    );
    return data;
  },

  changeFlatRoomBedStatus: async (
    hostelId, blockId, floorId, flatId, roomId, bedId, status
  ) => {
    const { data } = await axiosClient.patch(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms/${roomId}/beds/${bedId}/status`,
      null,
      { params: { status } }
    );
    return data;
  },

  // Hostel team
  getStaff: async (hostelId) => {
    const { data } = await axiosClient.get(
      `/api/hostel-admin/hostels/${hostelId}/staff`
    );
    return data;
  },

  createStaff: async (hostelId, payload) => {
    const { data } = await axiosClient.post(
      `/api/hostel-admin/hostels/${hostelId}/staff`,
      payload
    );
    return data;
  },

  // Allocations
  getAvailableBeds: async (hostelId) => {
  const { data } = await axiosClient.get(
    `/api/hostels/${encodeURIComponent(hostelId)}/available-beds`
  );
  return data;
},

getAllocations: async (hostelId) => {
  const { data } = await axiosClient.get(
    `/api/hostels/${encodeURIComponent(hostelId)}/allocations`
  );
  return data;
},

  createAllocation: async (hostelId, payload) => {
  const { data } = await axiosClient.post(
    `/api/hostels/${encodeURIComponent(hostelId)}/allocations`,
    payload
  );
  return data;
},
checkInAllocation: async (hostelId, allocationId, checkInDate) => {
  const { data } = await axiosClient.patch(
    `/api/hostels/${encodeURIComponent(hostelId)}/allocations/${encodeURIComponent(allocationId)}/check-in`,
    { checkInDate }
  );
  return data;
},

checkOutAllocation: async (hostelId, allocationId, checkOutDate) => {
  const { data } = await axiosClient.patch(
    `/api/hostels/${encodeURIComponent(hostelId)}/allocations/${encodeURIComponent(allocationId)}/check-out`,
    { checkOutDate }
  );
  return data;
},

cancelAllocation: async (hostelId, allocationId) => {
  const { data } = await axiosClient.delete(
    `/api/hostels/${encodeURIComponent(hostelId)}/allocations/${encodeURIComponent(allocationId)}`
  );
  return data;
},

  // Read-only pricing
  getPricing: async (hostelId) => {
    const { data } = await axiosClient.get(
      `/api/hostels/${hostelId}/pricing`
    );
    return data;
  },

  // Signed-in hostler's own billing data; not hostel-wide admin data
  getMyPayments: async () => {
    const { data } = await axiosClient.get("/api/payments/mine");
    return data;
  },

  getMyInvoices: async () => {
    const { data } = await axiosClient.get("/api/billing/invoices/mine");
    return data;
  },
  
  // Delete a block
  deleteBlock: async (hostelId, blockId) => {
    const { data } = await axiosClient.delete(
      `/api/hostels/${hostelId}/blocks/${blockId}`
    );
    return data;
  },

  // Delete a floor
  deleteFloor: async (hostelId, blockId, floorId) => {
    const { data } = await axiosClient.delete(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}`
    );
    return data;
  },

  // Delete a flat
  deleteFlat: async (hostelId, blockId, floorId, flatId) => {
    const { data } = await axiosClient.delete(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}`
    );
    return data;
  },

  // Delete a room directly under a floor
  deleteRoom: async (hostelId, blockId, floorId, roomId) => {
    const { data } = await axiosClient.delete(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms/${roomId}`
    );
    return data;
  },

  // Delete a room inside a flat
  deleteFlatRoom: async (
    hostelId, blockId, floorId, flatId, roomId
  ) => {
    const { data } = await axiosClient.delete(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms/${roomId}`
    );
    return data;
  },

  // Delete a bed directly under a floor room
  deleteBed: async (hostelId, blockId, floorId, roomId, bedId) => {
    const { data } = await axiosClient.delete(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms/${roomId}/beds/${bedId}`
    );
    return data;
  },

  // Delete a bed inside a room belonging to a flat
  deleteFlatRoomBed: async (
    hostelId, blockId, floorId, flatId, roomId, bedId
  ) => {
    const { data } = await axiosClient.delete(
      `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms/${roomId}/beds/${bedId}`
    );
    return data;
  },
  
  // Food menu management
  
  async getMenu(hostelId) {
  const { data } = await axiosClient.get(
    `/api/hostel-admin/hostels/${hostelId}/menu`
  );
  return data;
},

async createMenuItem(hostelId, payload) {
  const { data } = await axiosClient.post(
    `/api/hostel-admin/hostels/${hostelId}/menu`,
    payload
  );
  return data;
},

async updateMenuItem(hostelId, itemId, payload) {
  const { data } = await axiosClient.put(
    `/api/hostel-admin/hostels/${hostelId}/menu/${itemId}`,
    payload
  );
  return data;
},

async deleteMenuItem(hostelId, itemId) {
  try {
    const response = await axiosClient.delete(
      `/api/hostel-admin/hostels/${hostelId}/menu/${itemId}`
    );

    console.log("Menu delete successful:", {
      hostelId,
      itemId,
      status: response.status,
      data: response.data,
    });

    return response;
  } catch (error) {
    console.error("Menu delete failed:", {
      hostelId,
      itemId,
      status: error.response?.status,
      data: error.response?.data,
      message: error.message,
    });

    throw error;
  }
},


};

export default hostelAdminService;
