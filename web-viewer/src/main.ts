import "./style.css";
import { cacheShare, listenShare, readCachedShare, readLastShareToken } from "./firebase";
import {
  DisplayController,
  parseShareTokenFromPath,
  renderError,
  renderLoading,
} from "./display";
import { renderLanding } from "./landing";
import { setupPwaInstall } from "./pwa-install";

const root = document.querySelector<HTMLElement>("#app");
if (!root) {
  throw new Error("#app missing");
}

let currentLanguage = "en";

const token = parseShareTokenFromPath(window.location.pathname);

if (!token) {
  const last = readLastShareToken();
  renderLanding(root, { lastSharePath: last ? `/s/${last}` : null });
} else {
  const pwa = setupPwaInstall(() => currentLanguage);
  const controller = new DisplayController(root);
  const cached = readCachedShare(token);
  if (cached) {
    currentLanguage = cached.config.language || "en";
    pwa.refreshLabel();
    controller.setShare(cached);
  } else {
    renderLoading(root, "Loading display…");
  }

  // Single-doc listener only — never screens/{id} (avoids heartbeat read amplification).
  const unsub = listenShare(
    token,
    (share) => {
      if (!share) {
        controller.destroy();
        renderError(root, "This public view is unavailable or has been revoked.");
        return;
      }
      currentLanguage = share.config.language || "en";
      pwa.refreshLabel();
      cacheShare(token, share);
      controller.setShare(share);
    },
    (err) => {
      console.error(err);
      if (!cached) {
        renderError(
          root,
          "Could not load this display. Check your connection and try again."
        );
      }
    }
  );

  window.addEventListener("beforeunload", () => {
    unsub();
    controller.destroy();
  });
}
