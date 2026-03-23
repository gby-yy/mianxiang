import { studentLogin, studentGetInfo, studentLogout } from '@/api/student'
import { getStudentToken, setStudentToken, removeStudentToken } from '@/utils/auth'
import CryptoJS from 'crypto-js'

const getDefaultState = () => ({
  token: getStudentToken(),
  id: '',
  name: '',
  avatar: '',
  role: 'student'
})

const state = getDefaultState()

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_ID: (state, id) => {
    state.id = id
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ROLE: (state, role) => {
    state.role = role
  }
}

const actions = {
  login({ commit }, { username, password }) {
    const encryptedPassword = CryptoJS.SHA256('intelligent_teaching_system' + password).toString()
    return new Promise((resolve, reject) => {
      studentLogin({
        username: (username || '').trim(),
        password: encryptedPassword
      }).then(response => {
        const { data } = response
        commit('RESET_STATE')
        commit('SET_TOKEN', data)
        setStudentToken(data)
        resolve()
      }).catch(err => reject(err))
    })
  },

  getInfo({ commit }) {
    return new Promise((resolve, reject) => {
      studentGetInfo().then(response => {
        const { data } = response
        if (!data) {
          return reject(new Error('校验失败，请重新登录'))
        }
        commit('SET_ID', data.id)
        commit('SET_NAME', data.name)
        commit('SET_AVATAR', data.avatar || '')
        commit('SET_ROLE', data.role || 'student')
        resolve(data)
      }).catch(err => reject(err))
    })
  },

  logout({ commit }) {
    return new Promise((resolve, reject) => {
      studentLogout().then(() => {
        removeStudentToken()
        commit('RESET_STATE')
        resolve()
      }).catch(err => reject(err))
    })
  },

  resetToken({ commit }) {
    return new Promise(resolve => {
      removeStudentToken()
      commit('RESET_STATE')
      resolve()
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
