import axiosClient from "./axiosClient";

const getBaseUrl = ({
  hostelId,
  blockId,
  floorId,
  flatId,
  roomId,
}) => {
  const base =
    `/api/hostels/${hostelId}` +
    `/blocks/${blockId}` +
    `/floors/${floorId}`;

  if (flatId !== null && flatId !== undefined && flatId !== "") {
    return `${base}/flats/${flatId}/rooms/${roomId}/beds`;
  }

  return `${base}/rooms/${roomId}/beds`;
};

const bedService = {
  getAll(params) {
    return axiosClient.get(getBaseUrl(params));
  },

  getById(params, bedId) {
    return axiosClient.get(`${getBaseUrl(params)}/${bedId}`);
  },

  create(params, payload) {
    return axiosClient.post(getBaseUrl(params), payload);
  },

  update(params, bedId, payload) {
    return axiosClient.put(
      `${getBaseUrl(params)}/${bedId}`,
      payload
    );
  },

  changeStatus(params, bedId, status) {
    return axiosClient.patch(
      `${getBaseUrl(params)}/${bedId}/status`,
      null,
      { params: { status } }
    );
  },
};

export default bedService;
