export const parseJwt = (token) => {
  if (!token) {
    return null;
  }

  const [, payload] = token.split(".");
  if (!payload) {
    throw new Error("Invalid JWT structure");
  }

  const adjusted = payload.replace(/-/g, "+").replace(/_/g, "/");
  const decoded = decodeURIComponent(
    atob(adjusted)
      .split("")
      .map((char) => `%${(`00${char.charCodeAt(0).toString(16)}`).slice(-2)}`)
      .join("")
  );

  return JSON.parse(decoded);
};

export default parseJwt;
