interface BeforeInstallPromptEvent extends Event {
  prompt(): Promise<void>;
  userChoice: Promise<{ outcome: "accepted" | "dismissed" }>;
}

function isStandalone(): boolean {
  return (
    window.matchMedia("(display-mode: standalone)").matches ||
    Boolean((navigator as Navigator & { standalone?: boolean }).standalone)
  );
}

function isIos(): boolean {
  return /iphone|ipad|ipod/i.test(navigator.userAgent);
}

export function setupPwaInstall(getLanguage: () => string): { refreshLabel: () => void } {
  const btn = document.querySelector<HTMLButtonElement>("#installPwa");
  const hint = document.querySelector<HTMLElement>("#installHint");
  const hintText = document.querySelector<HTMLElement>("#installHintText");
  const hintClose = document.querySelector<HTMLButtonElement>("#installHintClose");
  if (!btn) return { refreshLabel: () => undefined };

  if (isStandalone()) {
    btn.hidden = true;
    return { refreshLabel: () => undefined };
  }

  let deferred: BeforeInstallPromptEvent | null = null;

  const label = () => (getLanguage() === "bn" ? "অ্যাপ ইনস্টল" : "Install app");

  const refreshLabel = () => {
    const text = btn.querySelector(".install-label");
    if (text) text.textContent = label();
    btn.setAttribute("aria-label", label());
    if (hintClose) {
      hintClose.textContent = getLanguage() === "bn" ? "ঠিক আছে" : "OK";
    }
  };

  const showHint = () => {
    if (!hint || !hintText) return;
    const bn = getLanguage() === "bn";
    if (isIos()) {
      hintText.textContent = bn
        ? "শেয়ার বোতাম (□↑) চাপুন, তারপর Add to Home Screen বেছে নিন।"
        : "Tap the Share button, then choose Add to Home Screen.";
    } else {
      hintText.textContent = bn
        ? "ব্রাউজার মেনু খুলুন এবং Install app / Add to Home Screen বেছে নিন।"
        : "Open the browser menu and choose Install app or Add to Home Screen.";
    }
    hint.hidden = false;
  };

  window.addEventListener("beforeinstallprompt", (event) => {
    event.preventDefault();
    deferred = event as BeforeInstallPromptEvent;
    btn.hidden = false;
    refreshLabel();
  });

  window.addEventListener("appinstalled", () => {
    deferred = null;
    btn.hidden = true;
    if (hint) hint.hidden = true;
  });

  btn.addEventListener("click", async () => {
    if (deferred) {
      await deferred.prompt();
      await deferred.userChoice;
      deferred = null;
      return;
    }
    showHint();
  });

  hintClose?.addEventListener("click", () => {
    if (hint) hint.hidden = true;
  });

  btn.hidden = false;
  refreshLabel();
  return { refreshLabel };
}
