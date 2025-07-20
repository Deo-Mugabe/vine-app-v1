export const formatPath = (path) => {
  if (!path) return '';
  return path.trim().replace(/\\/g, '/');
};

export const maskPassword = (password) => {
  if (!password) return '';
  return '*'.repeat(password.length);
};
