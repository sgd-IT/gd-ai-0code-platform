/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_APP_PREVIEW_URL: string
  readonly VITE_APP_DEPLOY_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
