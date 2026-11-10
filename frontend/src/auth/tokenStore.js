// Pastreaza token-ul JWT intre reincarcari de pagina.
// localStorage poate fi citit de orice script din pagina (XSS), deci nu afisam niciodata
// HTML primit de la utilizatori fara escape (React face escape automat in JSX).
const KEY = 'supportchat.auth'

export function loadToken() {
  try {
    const saved = JSON.parse(localStorage.getItem(KEY))
    if (!saved?.accessToken || Date.now() >= saved.expiresAt) {
      clearToken()
      return null
    }
    return saved
  } catch {
    return null
  }
}

export function saveToken(accessToken, expiresInSeconds) {
  const saved = { accessToken, expiresAt: Date.now() + expiresInSeconds * 1000 }
  try {
    localStorage.setItem(KEY, JSON.stringify(saved))
  } catch {
    // stocare indisponibila (mod privat): token-ul ramane doar in memorie
  }
  return saved
}

export function clearToken() {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // ignorat
  }
}
