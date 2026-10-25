// Only a native session change owns the web token. Web-only login survives page loads.
((base, token, sid, dark, pageLoaded) => {
    if (location.origin !== base) return "none";
    const markerKey = "bit101_native_session";
    let store, previous;
    try { store = JSON.parse(localStorage.getItem("store")) || {}; } catch (_) { store = {}; }
    try { previous = JSON.parse(localStorage.getItem(markerKey)); } catch (_) { previous = null; }
    if (typeof store !== "object" || Array.isArray(store)) store = {};
    store.theme_mode = dark ? "dark" : "light";
    const nativeChanged = !previous || previous.token !== token || previous.sid !== sid;
    const replaceToken = nativeChanged && (token || (previous && previous.token));
    const webChanged = replaceToken && (store.fake_cookie || "") !== token;
    const accountChanged = !previous || previous.sid !== sid || !token;
    // Same-account renewal waits for navigation or an explicit refresh, preserving forms.
    if (webChanged && !accountChanged && !pageLoaded) return "refresh";
    if (replaceToken) {
        if (token) store.fake_cookie = token; else delete store.fake_cookie;
    }
    localStorage.setItem("store", JSON.stringify(store));
    localStorage.setItem(markerKey, JSON.stringify({ token, sid }));
    const invalidatePage = previous && (previous.sid !== sid || (previous.token && !token));
    return webChanged || invalidatePage ? "reload" : "none";
})
