<template>
  <div class="tree-node">
    <div 
      class="node-content"
      :class="{ 
        selected: selectedOrgan && selectedOrgan.organId === organ.organId,
        'has-children': hasChildren
      }"
      @click="selectNode"
      @contextmenu="showContextMenu"
    >
      <span 
        class="expand-icon"
        :class="{ expanded: isExpanded }"
        @click.stop="toggleExpand"
        v-if="hasChildren"
      >
        ▶
      </span>
      <span class="expand-placeholder" v-else></span>
      
      <i class="node-icon">🏢</i>
      <span class="node-label">{{ organ.organName }}</span>
    </div>

    <!-- 子节点 -->
    <div v-if="isExpanded && hasChildren" class="children">
      <TreeNode
        v-for="child in organ.children"
        :key="child.organId"
        :organ="child"
        :selected-organ="selectedOrgan"
        @select="$emit('select', $event)"
        @add-child="$emit('add-child', $event)"
        @rename="$emit('rename', $event)"
        @delete="$emit('delete', $event)"
        @show-context-menu="$emit('show-context-menu', $event)"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'TreeNode',
  props: {
    organ: {
      type: Object,
      required: true
    },
    selectedOrgan: {
      type: Object,
      default: null
    }
  },
  emits: ['select', 'add-child', 'rename', 'delete', 'show-context-menu'],
  data() {
    return {
      isExpanded: true // 默认展开
    }
  },
  computed: {
    /**
     * 是否有子节点
     */
    hasChildren() {
      return this.organ.children && this.organ.children.length > 0
    }
  },
  methods: {
    /**
     * 选择节点
     */
    selectNode() {
      this.$emit('select', this.organ)
    },

    /**
     * 切换展开/收起
     */
    toggleExpand() {
      if (this.hasChildren) {
        this.isExpanded = !this.isExpanded
      }
    },

    /**
     * 显示右键菜单
     */
    showContextMenu(event) {
      event.preventDefault()
      event.stopPropagation()
      
      // 先选中当前节点
      this.selectNode()
      
      // 通过事件向上抛出，由 OrganizationTree 统一处理
      this.$emit('show-context-menu', { organ: this.organ, event })
    }
  }
}
</script>

<style scoped>
.tree-node {
  user-select: none;
}

.node-content {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
  margin: 1px 0;
}

.node-content:hover {
  background: #f5f5f5;
}

.node-content.selected {
  background: #e6f7ff;
  color: #1890ff;
}

.expand-icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  font-size: 10px;
  cursor: pointer;
  transition: transform 0.3s;
  margin-right: 4px;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

.expand-placeholder {
  width: 20px;
  height: 16px;
  display: inline-block;
}

.node-icon {
  margin-right: 6px;
  font-size: 14px;
}

.node-label {
  font-size: 13px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.children {
  margin-left: 20px;
  border-left: 1px dashed #d9d9d9;
  padding-left: 8px;
}

/* 根节点样式调整 - 移除加粗效果 */
.tree-node:first-child > .node-content {
  font-weight: normal;
}
</style>