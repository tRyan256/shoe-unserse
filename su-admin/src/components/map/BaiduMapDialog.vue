<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="960px"
    destroy-on-close
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
  >
    <div class="toolbar">
      <el-input
        v-model="searchText"
        placeholder="输入地址后回车或点击搜索定位"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" :loading="searching" @click="handleSearch">搜索定位</el-button>
      <el-button v-if="mode === 'pick'" @click="handleLocateMe">定位到当前</el-button>
      <div class="coord">
        <span class="label">经度</span>
        <span class="value">{{ coordText.lng }}</span>
        <span class="label">纬度</span>
        <span class="value">{{ coordText.lat }}</span>
        <el-button v-if="coordText.lng && coordText.lat" link type="primary" @click="copyCoord">复制</el-button>
      </div>
    </div>

    <div class="map-wrap">
      <div ref="mapEl" class="map" />
      <div v-if="mode === 'pick'" class="hint">
        点击地图选择点位；可拖拽标记微调
      </div>
    </div>

    <div class="address-line" v-if="resolvedAddress">
      <span class="k">解析地址</span>
      <span class="v">{{ resolvedAddress }}</span>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button v-if="mode === 'pick'" type="primary" :disabled="!pickedPoint" @click="confirmPick">
        确认选点
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getLoadedBaiduMapProvider, loadBMapGL } from '@/utils/baiduMap'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '地图' },
  mode: { type: String, default: 'view' },
  longitude: { type: [Number, String], default: null },
  latitude: { type: [Number, String], default: null },
  address: { type: String, default: '' },
  city: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const mapEl = ref(null)
const map = ref(null)
const provider = ref(null)
const marker = ref(null)
const geocoder = ref(null)
const searching = ref(false)

const pickedPoint = ref(null)
const resolvedAddress = ref('')
const searchText = ref('')

const coordText = computed(() => {
  if (!pickedPoint.value) return { lng: '', lat: '' }
  return {
    lng: Number(pickedPoint.value.lng).toFixed(6),
    lat: Number(pickedPoint.value.lat).toFixed(6)
  }
})

function parseNumber(v) {
  if (v === null || v === undefined) return null
  const n = Number(v)
  return Number.isFinite(n) ? n : null
}

function setPoint(lng, lat, center = true) {
  if (!map.value || !provider.value) return
  const api = provider.value
  const point = new api.Point(lng, lat)
  pickedPoint.value = { lng, lat }

  if (marker.value) {
    marker.value.setPosition(point)
  } else {
    const mk = new api.Marker(point)
    if (props.mode === 'pick') {
      mk.enableDragging()
      mk.addEventListener('dragend', (e) => {
        if (e?.latlng) {
          setPoint(e.latlng.lng, e.latlng.lat, false)
          return
        }
        if (e?.point) {
          setPoint(e.point.lng, e.point.lat, false)
        }
      })
    }
    map.value.addOverlay(mk)
    marker.value = mk
  }

  if (center) {
    map.value.panTo(point)
  }

  if (geocoder.value) {
    geocoder.value.getLocation(point, (rs) => {
      resolvedAddress.value = rs?.address || ''
    })
  }
}

async function ensureMap() {
  provider.value = getLoadedBaiduMapProvider() || (await loadBMapGL())
  const api = provider.value
  if (!api) throw new Error('Baidu Map API not available')
  if (map.value) return

  const m = new api.Map(mapEl.value)
  m.enableScrollWheelZoom(true)
  if (api.ScaleControl) {
    m.addControl(new api.ScaleControl())
  }
  if (api.ZoomControl) {
    m.addControl(new api.ZoomControl())
  } else if (api.NavigationControl) {
    m.addControl(new api.NavigationControl())
  }
  map.value = m
  geocoder.value = api.Geocoder ? new api.Geocoder() : null

  if (props.mode === 'pick') {
    m.addEventListener('click', (e) => {
      if (e?.latlng) {
        setPoint(e.latlng.lng, e.latlng.lat)
        return
      }
      if (e?.point) {
        setPoint(e.point.lng, e.point.lat)
      }
    })
  }
}

async function initView() {
  await nextTick()
  await ensureMap()

  const lng = parseNumber(props.longitude)
  const lat = parseNumber(props.latitude)
  if (lng !== null && lat !== null) {
    const api = provider.value
    map.value.centerAndZoom(new api.Point(lng, lat), 16)
    setPoint(lng, lat, false)
    return
  }

  const api = provider.value
  const fallback = new api.Point(116.404, 39.915)
  map.value.centerAndZoom(fallback, 12)

  if (props.address) {
    await handleSearch(props.address)
  }
}

function kickResize() {
  if (!map.value) return
  let tries = 0
  const run = () => {
    tries += 1
    try {
      if (map.value.checkResize) {
        map.value.checkResize()
      }
    } catch (e) {
    }
    if (tries < 8) {
      setTimeout(run, 120)
    }
  }
  setTimeout(run, 0)
}

async function handleOpen() {
  resolvedAddress.value = ''
  searchText.value = props.address || ''
  pickedPoint.value = null
  if (marker.value && map.value) {
    try {
      map.value.removeOverlay(marker.value)
    } catch (e) {
    }
    marker.value = null
  }
}

async function handleOpened() {
  try {
    await initView()
    kickResize()
  } catch (e) {
    ElMessage.error(e?.message || '地图加载失败')
  }
}

function handleClose() {
  resolvedAddress.value = ''
  searching.value = false
  try {
    if (map.value && map.value.clearOverlays) {
      map.value.clearOverlays()
    }
  } catch (e) {
  }
  map.value = null
  marker.value = null
  geocoder.value = null
  pickedPoint.value = null
}

async function handleSearch(text) {
  if (!geocoder.value) return
  const keyword = (text ?? searchText.value ?? '').trim()
  if (!keyword) {
    ElMessage.warning('请输入地址')
    return
  }
  searching.value = true
  try {
    geocoder.value.getPoint(
      keyword,
      (pt) => {
        if (!pt) {
          ElMessage.warning('未找到匹配位置')
          return
        }
        map.value.centerAndZoom(pt, 16)
        setPoint(pt.lng, pt.lat, false)
      },
      props.city || ''
    )
  } finally {
    searching.value = false
  }
}

function handleLocateMe() {
  if (!map.value || !provider.value) return
  const api = provider.value
  if (!api.Geolocation) return
  const geo = new api.Geolocation()
  geo.getCurrentPosition((r) => {
    if (geo.getStatus && geo.getStatus() !== window.BMAP_STATUS_SUCCESS) {
      ElMessage.warning('定位失败')
      return
    }
    if (!r?.point) {
      ElMessage.warning('定位失败')
      return
    }
    map.value.centerAndZoom(r.point, 16)
    setPoint(r.point.lng, r.point.lat, false)
  })
}

async function copyCoord() {
  if (!coordText.value.lng || !coordText.value.lat) return
  const text = `${coordText.value.lng},${coordText.value.lat}`
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch (e) {
    ElMessage.warning('复制失败')
  }
}

function confirmPick() {
  if (!pickedPoint.value) return
  emit('confirm', {
    longitude: Number(pickedPoint.value.lng),
    latitude: Number(pickedPoint.value.lat),
    address: resolvedAddress.value || ''
  })
  visible.value = false
}

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    nextTick(() => {
      if (map.value) {
        try {
          map.value.checkResize()
        } catch (e) {
        }
      }
    })
  }
)
</script>

<style lang="scss" scoped>
.toolbar {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.coord {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-end;
  white-space: nowrap;
}

.label {
  font-size: 12px;
  color: #909399;
}

.value {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  color: #303133;
}

.map-wrap {
  position: relative;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
}

.map {
  height: 520px;
  width: 100%;
  position: relative;
  background: #f5f7fa;
}

.hint {
  position: absolute;
  left: 12px;
  top: 12px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(235, 238, 245, 0.9);
  color: #303133;
  font-size: 12px;
  padding: 8px 10px;
  border-radius: 10px;
}

.address-line {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  align-items: baseline;
}

.k {
  font-size: 12px;
  color: #909399;
  min-width: 56px;
}

.v {
  color: #303133;
  line-height: 1.6;
}
</style>
