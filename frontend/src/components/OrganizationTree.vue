<template>
  <div class="organization-tree">
    <div class="tree-node-list">
      <TreeNode
        v-for="organ in organizations"
        :key="organ.organId"
        :organ="organ"
        :selected-organ="selectedOrgan"
        @select="$emit('select-organ', $event)"
        @add-child="$emit('add-child', $event)"
        @rename="$emit('rename-organ', $event)"
        @delete="$emit('delete-organ', $event)"
        @show-context-menu="showContextMenuFor($event.organ, $event.event)"
      />
    </div>

    <!-- 右键菜单 -->
    <div 
      v-if="showContextMenu" 
      class="context-menu"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
      @click.stop
    >
      <div class="menu-item" @click="addChildOrgan">
        <i class="icon-plus"></i>
        新增子机构
      </div>
      <div class="menu-item" @click="renameOrgan">
        <i class="icon-edit"></i>
        重命名
      </div>
      <div class="menu-item danger" @click="deleteOrgan">
        <i class="icon-delete"></i>
        删除
      </div>
    </div>

    <!-- 遮罩层，用于关闭右键菜单 -->
    <div 
      v-if="showContextMenu" 
      class="context-menu-overlay"
      @click="closeContextMenu"
    ></div>
  </div>
</template>

<script>
import TreeNode from './TreeNode.vue'

export default {
  name: 'OrganizationTree',
  components: {
    TreeNode
  },
  props: {
    organizations: {
      type: Array,
      default: () => []
    },
    selectedOrgan: {
      type: Object,
      default: null
    }
  },
  emits: ['select-organ', 'add-child', 'rename-organ', 'delete-organ'],
  data() {
    return {
      showContextMenu: false,
      contextMenuX: 0,
      contextMenuY: 0,
      contextMenuOrgan: null
    }
  },
  mounted() {
    // 监听全局点击事件，关闭右键菜单
    document.addEventListener('click', this.closeContextMenu)
    document.addEventListener('contextmenu', this.closeContextMenu)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.closeContextMenu)
    document.removeEventListener('contextmenu', this.closeContextMenu)
  },
  methods: {
    /**
     * 显示右键菜单
     */
    showContextMenuFor(organ, event) {
      event.preventDefault()
      event.stopPropagation()
      
      this.contextMenuOrgan = organ
      this.contextMenuX = event.clientX
      this.contextMenuY = event.clientY
      this.showContextMenu = true
    },

    /**
     * 关闭右键菜单
     */
    closeContextMenu() {
      this.showContextMenu = false
      this.contextMenuOrgan = null
    },

    /**
     * 新增子机构
     */
    addChildOrgan() {
      this.$emit('add-child', this.contextMenuOrgan)
      this.closeContextMenu()
    },

    /**
     * 重命名机构
     */
    renameOrgan() {
      this.$emit('rename-organ', this.contextMenuOrgan)
      this.closeContextMenu()
    },

    /**
     * 删除机构
     */
    deleteOrgan() {
      this.$emit('delete-organ', this.contextMenuOrgan)
      this.closeContextMenu()
    }
  }
}
</script>

<style scoped>
.organization-tree {
  position: relative;
  height: 100%;
}

.tree-node-list {
  height: 100%;
  overflow-y: auto;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  background: white;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  min-width: 120px;
  padding: 4px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 13px;
  color: #262626;
  transition: background-color 0.3s;
}

.menu-item:hover {
  background: #f5f5f5;
}

.menu-item.danger {
  color: #ff4d4f;
}

.menu-item.danger:hover {
  background: #fff2f0;
}

.menu-item i {
  font-size: 14px;
  width: 14px;
}

/* 遮罩层 */
.context-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

/* 图标 */
.icon-plus::before {
  content: '+';
}

.icon-edit::before {
  content: '✏️';
}

.icon-delete::before {
  content: '🗑️';
}
</style>