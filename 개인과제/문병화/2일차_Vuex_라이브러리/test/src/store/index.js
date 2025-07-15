// store/index.js
import { createStore } from 'vuex'

export default createStore({
  state() {
    return {
      count: 0
    }
  },
  getters: {
    double(state) {
      return state.count * 2
    }
  },
  mutations: {
    increment(state) {
      state.count++
    }
  },
  actions: {
    delayedIncrement({ commit }) {
      setTimeout(() => {
        commit('increment')
      }, 500)
    }
  }
})
