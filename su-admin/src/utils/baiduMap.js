let loadingPromise = null
let loadedProvider = null

async function resolveAk(explicitAk) {
  const direct = explicitAk || import.meta.env.VITE_BAIDU_MAP_AK
  if (direct) return direct
  try {
    const { default: request } = await import('@/utils/request')
    const ak = await request({
      url: '/admin/map/ak',
      method: 'get'
    })
    return ak
  } catch (e) {
    return null
  }
}

function detectProvider() {
  if (typeof window === 'undefined') return null
  return window.BMapGL || window.BMap || null
}

function ensureBMapCss() {
  const id = 'baidu-map-webgl-css'
  if (document.getElementById(id)) return
  const link = document.createElement('link')
  link.id = id
  link.rel = 'stylesheet'
  link.type = 'text/css'
  link.href = 'https://api.map.baidu.com/res/webgl/10/bmap.css'
  document.head.appendChild(link)
}

export async function loadBMapGL(ak) {
  const exist = detectProvider()
  if (exist) {
    loadedProvider = exist
    return Promise.resolve(exist)
  }
  if (loadingPromise) return loadingPromise
  const resolvedAk = await resolveAk(ak)
  if (!resolvedAk) {
    return Promise.reject(new Error('Missing Baidu Map AK'))
  }

  loadingPromise = new Promise((resolve, reject) => {
    const existing = document.querySelector('script[data-baidu-map="bmapgl"]')
    if (existing) {
      existing.addEventListener('load', () => resolve(window.BMapGL))
      existing.addEventListener('error', () => reject(new Error('Failed to load Baidu Map script')))
      return
    }

    ensureBMapCss()
    window.BMAP_PROTOCOL = 'https'

    const script = document.createElement('script')
    script.async = true
    script.defer = true
    script.dataset.baiduMap = 'bmapgl'
    script.src = `https://api.map.baidu.com/getscript?type=webgl&v=1.0&ak=${encodeURIComponent(resolvedAk)}&services=&t=${Date.now()}`
    script.onload = () => {
      const start = Date.now()
      const poll = () => {
        const provider = detectProvider()
        if (provider) {
          loadedProvider = provider
          resolve(provider)
          return
        }
        if (Date.now() - start > 2000) {
          reject(
            new Error(
              'BMapGL not found after script loaded: 请检查 AK 类型、Referer 白名单与服务开通情况'
            )
          )
          return
        }
        setTimeout(poll, 50)
      }
      poll()
    }
    script.onerror = () => {
      loadingPromise = null
      reject(new Error('Failed to load Baidu Map script'))
    }
    document.head.appendChild(script)
  })

  return loadingPromise
}

export function getLoadedBaiduMapProvider() {
  return loadedProvider || detectProvider()
}
