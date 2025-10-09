import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import authService from "../services/authService";
import { parseJwt } from "../utils/jwt";

const AuthContext = createContext(null);

const buildUserFromToken = (token) => {
  if (!token) {
    return null;
  }

  try {
    const decoded = parseJwt(token);
    const rawRoles = decoded?.roles;
    const roles = Array.isArray(rawRoles)
      ? rawRoles.map((role) => (typeof role === "string" ? role : role?.authority)).filter(Boolean)
      : typeof rawRoles === "string"
        ? rawRoles.split(",").map((role) => role.trim()).filter(Boolean)
        : [];

    return {
      username: decoded?.sub ?? null,
      roles,
      exp: decoded?.exp ? decoded.exp * 1000 : null
    };
  } catch (error) {
    console.warn("Failed to decode access token", error);
    return null;
  }
};

const updateStoredTokens = (access, refresh) => {
  if (access) {
    localStorage.setItem("accessToken", access);
  } else {
    localStorage.removeItem("accessToken");
  }

  if (refresh) {
    localStorage.setItem("refreshToken", refresh);
  } else {
    localStorage.removeItem("refreshToken");
  }

  window.dispatchEvent(new Event("tokensUpdated"));
};

export const AuthProvider = ({ children }) => {
  const [accessToken, setAccessToken] = useState(() => localStorage.getItem("accessToken"));
  const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem("refreshToken"));
  const [user, setUser] = useState(() => buildUserFromToken(localStorage.getItem("accessToken")));
  const [loadingProfile, setLoadingProfile] = useState(false);
  const [profileError, setProfileError] = useState(null);

  const logout = useCallback(() => {
    updateStoredTokens(null, null);
    setAccessToken(null);
    setRefreshToken(null);
    setUser(null);
  }, []);

  const loadProfile = useCallback(async () => {
    if (!accessToken) {
      return;
    }

    setLoadingProfile(true);
    setProfileError(null);
    try {
      const profile = await authService.fetchProfile();
      setUser((current) => {
        const base = current ?? {};
        return {
          ...base,
          customerId: profile?.customerId ?? base.customerId ?? null,
          username: profile?.username ?? base.username ?? null,
          roles: profile?.roles?.length ? profile.roles : base.roles ?? [],
          email: profile?.email ?? base.email ?? null
        };
      });
    } catch (error) {
      console.warn("Unable to fetch authenticated profile", error);
      setProfileError(error?.response?.data?.message ?? "Unable to load profile");
    } finally {
      setLoadingProfile(false);
    }
  }, [accessToken]);

  useEffect(() => {
    const decoded = buildUserFromToken(accessToken);
    if (decoded) {
      setUser((current) => ({ ...decoded, customerId: current?.customerId ?? decoded?.customerId ?? null }));
      loadProfile().catch(() => undefined);
    } else {
      setUser(null);
    }
  }, [accessToken, loadProfile]);

  useEffect(() => {
    const syncTokens = () => {
      setAccessToken(localStorage.getItem("accessToken"));
      setRefreshToken(localStorage.getItem("refreshToken"));
    };

    window.addEventListener("storage", syncTokens);
    window.addEventListener("tokensUpdated", syncTokens);
    return () => {
      window.removeEventListener("storage", syncTokens);
      window.removeEventListener("tokensUpdated", syncTokens);
    };
  }, []);

  useEffect(() => {
    const handleForcedLogout = () => logout();
    window.addEventListener("auth:logout", handleForcedLogout);
    return () => window.removeEventListener("auth:logout", handleForcedLogout);
  }, [logout]);

  const login = useCallback(
    async (credentials) => {
      const tokens = await authService.login(credentials);
      updateStoredTokens(tokens.accessToken, tokens.refreshToken);
      setAccessToken(tokens.accessToken);
      setRefreshToken(tokens.refreshToken);
      setUser(buildUserFromToken(tokens.accessToken));
      await loadProfile();
      return tokens;
    },
    [loadProfile]
  );

  const register = useCallback((payload) => authService.register(payload), []);

  const value = useMemo(
    () => ({
      accessToken,
      refreshToken,
      user,
      loadingProfile,
      profileError,
      login,
      logout,
      register
    }),
    [accessToken, refreshToken, user, loadingProfile, profileError, login, logout, register]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuthContext = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuthContext must be used within an AuthProvider");
  }
  return context;
};

export default AuthContext;
