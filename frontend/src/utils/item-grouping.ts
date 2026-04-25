export type ItemGroup<T> = {
  key: string;
  label: string;
  items: T[];
  showHeader: boolean;
  collapsible: boolean;
  defaultCollapsed: boolean;
};

export type GroupDefinition<T> = {
  key: string;
  label: string;
  predicate: (item: T) => boolean;
  comparator?: (a: T, b: T) => number;
  showHeader?: boolean;
  collapsible?: boolean;
  defaultCollapsed?: boolean;
};

/**
 * Groups items by the given definitions, preserving definition order.
 * Empty groups are excluded from the result.
 */
export function groupItems<T>(items: T[], definitions: GroupDefinition<T>[]): ItemGroup<T>[] {
  return definitions
    .map((def) => {
      const matched = items.filter(def.predicate);
      return {
        key: def.key,
        label: def.label,
        items: def.comparator ? [...matched].sort(def.comparator) : matched,
        showHeader: def.showHeader ?? true,
        collapsible: def.collapsible ?? false,
        defaultCollapsed: def.defaultCollapsed ?? false
      };
    })
    .filter((group) => group.items.length > 0);
}
