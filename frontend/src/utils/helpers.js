export const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

export const isEmptyObject = (obj) =>
  obj && Object.keys(obj).length === 0 && obj.constructor === Object;
