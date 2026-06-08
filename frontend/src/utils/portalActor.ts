const GUEST_KEY = "elder.portalGuestId";

export function getPortalActorId(): string {
  const userId = localStorage.getItem("elder.userId");
  if (userId) return userId;
  let guest = sessionStorage.getItem(GUEST_KEY);
  if (!guest) {
    guest = `guest-${crypto.randomUUID().slice(0, 8)}`;
    sessionStorage.setItem(GUEST_KEY, guest);
  }
  return guest;
}

export function getDefaultContactName(): string {
  return localStorage.getItem("elder.userName") ?? localStorage.getItem("elder.accountName") ?? "";
}
