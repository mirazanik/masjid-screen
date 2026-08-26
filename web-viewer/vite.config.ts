import { defineConfig } from "vite";
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig({
  plugins: [
    VitePWA({
      registerType: "autoUpdate",
      includeAssets: [
        "favicon.svg",
        "favicon-32.png",
        "icons/icon-192.png",
        "icons/icon-512.png",
        "images/feature-graphic.png",
      ],
      manifest: {
        name: "Masjid Screen",
        short_name: "Masjid Screen",
        description: "View mosque prayer display content on your phone",
        theme_color: "#050D1A",
        background_color: "#050D1A",
        display: "standalone",
        orientation: "any",
        start_url: "/",
        icons: [
          {
            src: "icons/icon-192.png",
            sizes: "192x192",
            type: "image/png",
          },
          {
            src: "icons/icon-512.png",
            sizes: "512x512",
            type: "image/png",
          },
          {
            src: "icons/icon-512.png",
            sizes: "512x512",
            type: "image/png",
            purpose: "maskable",
          },
        ],
      },
      workbox: {
        navigateFallback: "/index.html",
        runtimeCaching: [
          {
            urlPattern: ({ url }) =>
              url.pathname.includes("/publicShares/") ||
              url.hostname.includes("firestore.googleapis.com"),
            handler: "NetworkFirst",
            options: {
              cacheName: "share-snapshot",
              networkTimeoutSeconds: 8,
              expiration: {
                maxEntries: 8,
                maxAgeSeconds: 60 * 60 * 24,
              },
            },
          },
        ],
      },
    }),
  ],
});
