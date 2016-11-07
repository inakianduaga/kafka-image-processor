export default {
  defaults: {
    images: {
      frequency: 3,
      maxUpload: 10,
      size: 100,
    }
  },
  websocketEndpoint: process.env.BACKEND_ENDPOINT,
};
