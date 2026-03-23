import defaultSettings from '@/settings'

const title = defaultSettings.title || 'Vue Admin Template'

export default function getPageTitle(pageTitle) {
  if (pageTitle) {
    return `${pageTitle} - 面向计算机专业的个性化交互式智能教学系统`
  }
  return `面向计算机专业的个性化交互式智能教学系统`
}
