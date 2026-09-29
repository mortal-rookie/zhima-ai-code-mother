import type { Directive } from 'vue'

const cleanups = new WeakMap<HTMLElement, () => void>()

/** Pointer motion writes only CSS variables; no Vue updates or permanent frame loop. */
export const motionSurface: Directive<HTMLElement> = {
  mounted(element) {
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)')
    const fine = window.matchMedia('(hover: hover) and (pointer: fine)')
    let frame = 0
    let lastTime = 0
    let bounds: DOMRect | undefined
    let x = 0
    let y = 0
    let targetX = 0
    let targetY = 0
    let pointerX = 50
    let pointerY = 50
    const enabled = () => !reduced.matches && fine.matches && !document.hidden
    const paint = () => {
      element.style.setProperty('--motion-x', `${(x * 14).toFixed(3)}px`)
      element.style.setProperty('--motion-y', `${(y * 10).toFixed(3)}px`)
      element.style.setProperty('--tilt-x', `${(-y * 4).toFixed(3)}deg`)
      element.style.setProperty('--tilt-y', `${(x * 5).toFixed(3)}deg`)
      element.style.setProperty('--pointer-x', `${pointerX.toFixed(2)}%`)
      element.style.setProperty('--pointer-y', `${pointerY.toFixed(2)}%`)
    }
    const tick = (time: number) => {
      frame = 0
      if (!enabled()) {
        reset()
        return
      }
      const delta = lastTime ? Math.min(time - lastTime, 50) : 16
      lastTime = time
      const ease = 1 - Math.exp(-delta / 105)
      x += (targetX - x) * ease
      y += (targetY - y) * ease
      paint()
      if (Math.abs(targetX - x) + Math.abs(targetY - y) > 0.001) {
        frame = requestAnimationFrame(tick)
      } else {
        lastTime = 0
      }
    }
    const start = () => {
      if (!frame) frame = requestAnimationFrame(tick)
    }
    function reset() {
      cancelAnimationFrame(frame)
      frame = lastTime = x = y = targetX = targetY = 0
      bounds = undefined
      element.classList.remove('is-pointer-active')
      paint()
    }
    const enter = (event: PointerEvent) => {
      if (!enabled() || event.pointerType !== 'mouse') return
      bounds = element.getBoundingClientRect()
      element.classList.add('is-pointer-active')
    }
    const move = (event: PointerEvent) => {
      if (!bounds || !enabled() || event.pointerType !== 'mouse') return
      pointerX = Math.max(0, Math.min(100, ((event.clientX - bounds.left) / bounds.width) * 100))
      pointerY = Math.max(0, Math.min(100, ((event.clientY - bounds.top) / bounds.height) * 100))
      targetX = pointerX / 50 - 1
      targetY = pointerY / 50 - 1
      start()
    }
    const leave = () => {
      bounds = undefined
      targetX = targetY = 0
      element.classList.remove('is-pointer-active')
      if (enabled()) start()
      else reset()
    }
    const visibility = () => {
      if (document.hidden) reset()
    }
    const scroll = () => {
      if (bounds) bounds = element.getBoundingClientRect()
    }
    element.addEventListener('pointerenter', enter, { passive: true })
    element.addEventListener('pointermove', move, { passive: true })
    element.addEventListener('pointerleave', leave, { passive: true })
    window.addEventListener('scroll', scroll, { passive: true })
    window.addEventListener('resize', leave)
    document.addEventListener('visibilitychange', visibility)
    reduced.addEventListener('change', reset)
    fine.addEventListener('change', reset)
    paint()
    cleanups.set(element, () => {
      reset()
      element.removeEventListener('pointerenter', enter)
      element.removeEventListener('pointermove', move)
      element.removeEventListener('pointerleave', leave)
      window.removeEventListener('scroll', scroll)
      window.removeEventListener('resize', leave)
      document.removeEventListener('visibilitychange', visibility)
      reduced.removeEventListener('change', reset)
      fine.removeEventListener('change', reset)
    })
  },
  unmounted(element) {
    cleanups.get(element)?.()
    cleanups.delete(element)
  },
}
