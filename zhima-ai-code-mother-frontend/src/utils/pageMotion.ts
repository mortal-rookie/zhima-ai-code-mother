import { onMounted, onBeforeUnmount, type Ref } from 'vue'

/** Observe newly rendered content too, including API lists and filtered templates. */
export function usePageMotion(root: Ref<HTMLElement | undefined>) {
  let dispose = () => {}
  onMounted(() => {
    const surface = root.value
    if (!surface) return
    const reduced = matchMedia('(prefers-reduced-motion: reduce)')
    const tracked = new WeakSet<Element>()
    const waves = new Set<HTMLElement>()
    const selectors =
      '.section-heading, .empty-projects, .quiet-empty, .app-grid, .shortcut-grid, .template-art, .template-info, .guide-list > section, .resource-links, .creation-steps > div, .welcome-bottom'
    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (!entry.isIntersecting) continue
          entry.target.classList.remove('reveal-pending')
          observer.unobserve(entry.target)
        }
      },
      { threshold: 0.12, rootMargin: '0px 0px -24px 0px' },
    )
    const scan = () => {
      surface.querySelectorAll<HTMLElement>(selectors).forEach((element, index) => {
        if (tracked.has(element)) return
        tracked.add(element)
        if (reduced.matches) return
        element.style.setProperty('--reveal-delay', `${(index % 4) * 65}ms`)
        element.classList.add('scroll-reveal', 'reveal-pending')
        observer.observe(element)
      })
    }
    const changes = new MutationObserver(scan)
    changes.observe(surface, { childList: true, subtree: true })
    const showAll = () => {
      if (!reduced.matches) return
      surface
        .querySelectorAll('.reveal-pending')
        .forEach((element) => element.classList.remove('reveal-pending'))
      observer.disconnect()
    }
    const revealFocused = (event: FocusEvent) => {
      if (!(event.target instanceof Element)) return
      const element = event.target.closest('.reveal-pending')
      element?.classList.remove('reveal-pending')
      if (element) observer.unobserve(element)
    }
    const press = (event: PointerEvent | KeyboardEvent) => {
      if (reduced.matches || !(event.target instanceof Element)) return
      if (event instanceof KeyboardEvent && (event.repeat || !['Enter', ' '].includes(event.key)))
        return
      if (event instanceof PointerEvent && event.button !== 0) return
      const element = event.target.closest<HTMLElement>(
        '.dark-button, .send-button, .nav-link, .shortcut, .template-card, .category-tabs button, .guide-list button',
      )
      if (!element || element.matches(':disabled')) return
      const wave = document.createElement('span')
      wave.className = 'press-wave'
      wave.setAttribute('aria-hidden', 'true')
      if (event instanceof PointerEvent) {
        const rect = element.getBoundingClientRect()
        wave.style.setProperty('--press-x', `${((event.clientX - rect.left) / rect.width) * 100}%`)
        wave.style.setProperty('--press-y', `${((event.clientY - rect.top) / rect.height) * 100}%`)
      }
      element.append(wave)
      waves.add(wave)
      const animation = wave.animate(
        [
          { opacity: 0, scale: '.75' },
          { opacity: 1, scale: '1', offset: 0.22 },
          { opacity: 0, scale: '1.08' },
        ],
        { duration: 550, easing: 'cubic-bezier(.22,1,.36,1)' },
      )
      void animation.finished
        .catch(() => {})
        .finally(() => {
          wave.remove()
          waves.delete(wave)
        })
    }
    surface.addEventListener('pointerdown', press, { passive: true })
    surface.addEventListener('keydown', press)
    surface.addEventListener('focusin', revealFocused)
    reduced.addEventListener('change', showAll)
    scan()
    dispose = () => {
      observer.disconnect()
      changes.disconnect()
      surface.removeEventListener('pointerdown', press)
      surface.removeEventListener('keydown', press)
      surface.removeEventListener('focusin', revealFocused)
      reduced.removeEventListener('change', showAll)
      waves.forEach((wave) => {
        wave.getAnimations().forEach((animation) => animation.cancel())
        wave.remove()
      })
    }
  })
  onBeforeUnmount(() => dispose())
}
