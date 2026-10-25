const assert = require('node:assert/strict');
const { test } = require('node:test');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const source = fs.readFileSync(path.join(__dirname, '../../main/assets/bit101-session.js'), 'utf8');

function page(store = {}, marker, origin = 'https://bit101.cn') {
  const values = new Map([['store', JSON.stringify(store)]]);
  if (marker !== undefined) values.set('bit101_native_session', JSON.stringify(marker));
  const context = vm.createContext({ location: { origin }, localStorage: {
    getItem: key => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, value),
  }});
  const sync = vm.runInContext(source, context);
  return {
    sync: (token, sid = 'sid', loaded = false) => sync('https://bit101.cn', token, sid, false, loaded),
    store: () => JSON.parse(values.get('store')),
    marker: () => JSON.parse(values.get('bit101_native_session') ?? 'null'),
    webLogin: token => values.set('store', JSON.stringify({ ...JSON.parse(values.get('store')), fake_cookie: token })),
  };
}

test('native logged-out state preserves web login registration and password reset across full navigation', () => {
  const p = page({ fake_cookie: 'web-login' });
  assert.equal(p.sync('', '', true), 'none');
  p.webLogin('reset-password-token');
  assert.equal(p.sync('', '', true), 'none');
  assert.equal(p.store().fake_cookie, 'reset-password-token');
});

test('same native token does not repeatedly reinsert a token rejected by the website', () => {
  const p = page();
  assert.equal(p.sync('native'), 'reload');
  p.webLogin('');
  assert.equal(p.sync('native', 'sid', true), 'none');
  assert.equal(p.store().fake_cookie, '');
});

test('same-account renewal waits for explicit refresh without replacing active form session', () => {
  const p = page({ fake_cookie: 'old', last_draft: { body: 'draft' } }, { token: 'old', sid: 'sid' });
  assert.equal(p.sync('new'), 'refresh');
  assert.equal(p.store().fake_cookie, 'old');
  assert.equal(p.marker().token, 'old');
  assert.equal(p.sync('new', 'sid', true), 'reload');
  assert.equal(p.store().fake_cookie, 'new');
  assert.deepEqual(p.store().last_draft, { body: 'draft' });
  assert.equal(p.sync('new', 'sid', true), 'none');
});

test('logout immediately invalidates the loaded page and clears its token', () => {
  const p = page({ fake_cookie: 'web-or-native' }, { token: 'native', sid: 'sid' });
  assert.equal(p.sync('', ''), 'reload');
  assert.equal(p.store().fake_cookie, undefined);
  assert.equal(p.sync('', '', true), 'none');
});

test('account switch invalidates the page rather than leaving the old reactive account active', () => {
  const p = page({ fake_cookie: 'old' }, { token: 'old', sid: 'old-sid' });
  assert.equal(p.sync('new', 'new-sid'), 'reload');
  assert.equal(p.store().fake_cookie, 'new');
});

test('foreign origins receive no token or mutation', () => {
  const p = page({ fake_cookie: 'web' }, undefined, 'https://bit101.cn.evil.test');
  assert.equal(p.sync('secret'), 'none');
  assert.equal(p.store().fake_cookie, 'web');
  assert.equal(p.marker(), null);
});

test('logout invalidates reactive memory even if storage was already cleared', () => {
  const p = page({}, { token: 'native', sid: 'sid' });
  assert.equal(p.sync('', ''), 'reload');
});
