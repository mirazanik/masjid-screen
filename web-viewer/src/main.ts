import "./style.css";
import { cacheShare, listenShare, readCachedShare } from "./firebase";
import {
  DisplayController,
  parseShareTokenFromPath,
  renderError,
  renderLoading,
} from "./display";

const root = document.querySelector<HTMLElement>("#app");
if (!root) {
  throw new Error("#app missing");
}

const token = parseShareTokenFromPath(window.location.pathname);

if (!token) {
  renderError(
    root,
    "Open a share link from the MasjidScreen admin panel (or scan the QR code)."
  );
} else {
  const controller = new DisplayController(root);
  const cached = readCachedShare(token);
  if (cached) {
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
