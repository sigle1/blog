/** Public site registration details; configure with .env.local. */
export const siteIcp = {
  text: import.meta.env.VITE_SITE_ICP || '',
  href: 'https://beian.miit.gov.cn/'
}
export const sitePsb = {
  text: import.meta.env.VITE_SITE_PSB || '',
  href: import.meta.env.VITE_SITE_PSB_URL || ''
}