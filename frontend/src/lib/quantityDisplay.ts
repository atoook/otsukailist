import type { ItemCategory } from '@/types/item-category';
import { UNIT_DEFINITIONS, type BaseUnit, type UnitCode } from '@/types/list-generation';

type FormatQuantityInput = {
  quantity: number;
  baseUnit: BaseUnit;
  category: ItemCategory | null;
  displayUnit?: UnitCode;
};

export function formatQuantity({ quantity, baseUnit, category, displayUnit }: FormatQuantityInput): string {
  const resolvedDisplayUnit = resolveDisplayUnit({ quantity, baseUnit, displayUnit });
  const unitDefinition = UNIT_DEFINITIONS[resolvedDisplayUnit];

  if (!unitDefinition || unitDefinition.baseUnit !== baseUnit) {
    return `${quantity}${resolveUnitLabel(baseUnit, category)}`;
  }

  const value = quantity / unitDefinition.factor;
  const roundedValue = Math.round(value * 10) / 10;
  const formattedValue = Number.isInteger(roundedValue) ? String(roundedValue) : roundedValue.toFixed(1);
  return `${formattedValue}${resolveUnitLabel(resolvedDisplayUnit, category)}`;
}

function resolveDisplayUnit({ quantity, baseUnit, displayUnit }: Omit<FormatQuantityInput, 'category'>): UnitCode {
  if (baseUnit === 'g' && quantity >= 1000) {
    return 'kg';
  }
  if (baseUnit === 'ml' && quantity >= 1000) {
    return 'l';
  }
  return displayUnit ?? baseUnit;
}

export function resolveUnitLabel(unitCode: UnitCode, category: ItemCategory | null): string {
  if (unitCode === 'g') {
    return 'g';
  }
  if (unitCode === 'kg') {
    return 'kg';
  }
  if (unitCode === 'ml') {
    return 'ml';
  }
  if (unitCode === 'l') {
    return 'L';
  }
  if (unitCode === 'piece') {
    if (category === 'drinks') {
      return '本';
    }
    return '個';
  }
  if (unitCode === 'pack' && (category === 'meat' || category === 'seafood')) {
    return 'パック';
  }
  return '袋';
}
