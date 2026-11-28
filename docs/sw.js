/* Постійна адреса календаря; кеш змінюється під час кожного розгортання. */
const CACHE = 'upckp-site-__DEPLOY_REVISION__';
const PRECACHE = ['./index.html', './site.webmanifest', './icon.png', './icon-192.png', './icon-512.png'];

self.addEventListener('install', event => {
  event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(PRECACHE)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys => Promise.all(
      keys.filter(key => (key.startsWith('upckp-site-') || key.startsWith('upckp-html-')) && key !== CACHE)
        .map(key => caches.delete(key))
    )).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', event => {
  const request = event.request;
  if (request.method !== 'GET' || new URL(request.url).origin !== self.location.origin) return;

  // Online users always get the latest version; saved files work offline.
  event.respondWith((async () => {
    const cache = await caches.open(CACHE);
    try {
      const response = await fetch(request, {cache: 'no-store'});
      if (response.ok) {
        await cache.put(request, response.clone());
      }
      return response;
    } catch (error) {
      const cached = await cache.match(request, {ignoreSearch: false});
      if (cached) return cached;
      if (request.mode === 'navigate') {
        const home = await cache.match(new URL('index.html', self.registration.scope));
        if (home) return home;
      }
      return Response.error();
    }
  })());
});
