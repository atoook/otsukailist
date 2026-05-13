<template>
  <div class="relative">
    <select :id="selectId" :name="selectName" :class="selectClasses" :value="modelValue" @change="handleChange">
      <option v-for="optionItem in optionItems" :key="optionItem.id" :value="optionItem.id">
        {{ optionItem.name }}
      </option>
    </select>
    <span
      v-if="showArrow"
      class="absolute right-2 top-1/2 transform -translate-y-1/2 pointer-events-none text-wood-50 flex items-center"
    >
      <IconChevronDown />
    </span>
  </div>
</template>
<script>
import { twMerge } from 'tailwind-merge';
import IconChevronDown from './icons/IconChevronDown.vue';

function optionItemsValidator(value) {
  if (!Array.isArray(value)) {
    return false;
  }
  return value.every((item) => item && 'id' in item && 'name' in item);
}
export default {
  components: {
    IconChevronDown
  },
  data() {
    return {};
  },
  props: {
    selectId: {
      type: String,
      required: true
    },
    selectName: {
      type: String,
      required: true
    },
    modelValue: {
      type: String,
      default: null
    },
    optionItems: {
      type: Array,
      required: true,
      validator: optionItemsValidator
    },
    showArrow: {
      type: Boolean,
      default: true
    },
    width: {
      type: String,
      default: 'full',
      validator: (value) => ['full', 'fixed'].includes(value)
    }
  },
  emits: ['update:modelValue'],
  computed: {
    selectClasses() {
      const baseClasses =
        'border rounded-lg bg-wood-500 text-wood-50 pl-3 py-2 appearance-none focus:outline-none overflow-hidden whitespace-nowrap text-ellipsis';

      // 幅をwidthプロパティで制御
      const widthClasses = {
        full: 'w-full',
        fixed: 'w-28 min-[380px]:w-32'
      };

      const rightPadding = this.showArrow ? 'pr-8' : 'pr-3';
      return twMerge(baseClasses, widthClasses[this.width], rightPadding);
    }
  },
  methods: {
    handleChange(event) {
      const selectedValue = event.target.value;
      this.$emit('update:modelValue', selectedValue);
    }
  }
};
</script>
