import { BBQ_GENERATION_RULES } from '@/lib/listGenerationConstants';
import type { ItemCategory } from '@/types/item-category';
import type { BaseUnit, UnitCode } from '@/types/list-generation';

export type BBQGenerationLevel = 'small' | 'normal' | 'large';
export type BBQGenerationBalance = 'vegetables' | 'balanced' | 'meat';
export type BBQStapleLevel = 'none' | 'light' | 'solid';
export type BBQAlcoholLevel = 'none' | 'included';
export type BBQSeafoodLevel = 'none' | 'included';

export type BBQGenerationAnswers = {
  adultCount: number;
  childCount: number;
  appetite: BBQGenerationLevel;
  balance: BBQGenerationBalance;
  drinkLevel: BBQGenerationLevel;
  alcoholLevel: BBQAlcoholLevel;
  alcoholAmount: BBQGenerationLevel;
  seafoodLevel: BBQSeafoodLevel;
  stapleLevel: BBQStapleLevel;
};

export type BBQGenerationAdjustments = {
  meat: BBQGenerationLevel;
  seafood: BBQGenerationLevel;
  vegetables: BBQGenerationLevel;
  drinks: BBQGenerationLevel;
  staple: BBQGenerationLevel;
  overall: BBQGenerationLevel;
};

export type BBQGenerationConfig = {
  version: 1;
  answers: BBQGenerationAnswers;
  adjustments: BBQGenerationAdjustments;
};

export type GeneratedItemCandidate = {
  generatorKey: string;
  name: string;
  category: ItemCategory;
  quantity: number;
  baseUnit: BaseUnit;
  displayUnit: UnitCode;
};

type GenerateBBQItemsInput = {
  answers: BBQGenerationAnswers;
  adjustments: BBQGenerationAdjustments;
};

type BBQGeneratorKey = keyof typeof BBQ_GENERATION_RULES;

type BreakdownRule = {
  generatorKey: BBQGeneratorKey;
  amountPerBaseQuantity: number;
};

const LEVEL_FACTORS: Record<BBQGenerationLevel, number> = {
  small: 0.8,
  normal: 1,
  large: 1.25
};

const APPETITE_FACTORS: Record<BBQGenerationLevel, number> = {
  small: 0.8,
  normal: 1,
  large: 1.2
};

const BALANCE_FACTORS: Record<
  BBQGenerationBalance,
  { beef: number; pork: number; chicken: number; sausage: number; vegetables: number }
> = {
  vegetables: { beef: 0.85, pork: 0.85, chicken: 0.85, sausage: 0.8, vegetables: 1.35 },
  balanced: { beef: 1, pork: 1, chicken: 1, sausage: 1, vegetables: 1 },
  meat: { beef: 1.15, pork: 1.1, chicken: 1.1, sausage: 1.15, vegetables: 0.9 }
};

const CHILD_TO_ADULT_FACTOR = 0.6;
const BEEF_GRAMS_PER_PERSON = 110;
const PORK_GRAMS_PER_PERSON = 80;
const CHICKEN_GRAMS_PER_PERSON = 60;
const SAUSAGE_GRAMS_PER_PERSON = 40;
const SEAFOOD_GRAMS_PER_PERSON = 120;
const SEAFOOD_MEAT_REDUCTION_FACTOR = 0.85;
// Assumes a grill-friendly mix such as onion, bell pepper, corn, potato, and mushrooms.
const VEGETABLE_GRAMS_PER_PERSON = 150;
const SOFT_DRINK_ML_PER_PERSON = 500;
const ALCOHOL_ML_PER_ADULT = 500;
const ALCOHOL_CAN_ML = 350;

const VEGETABLE_BREAKDOWN_BASE_GRAMS = 600;
const VEGETABLE_BREAKDOWN_RULES: BreakdownRule[] = [
  { generatorKey: 'vegetableOnion', amountPerBaseQuantity: 1 },
  { generatorKey: 'vegetableBellPepper', amountPerBaseQuantity: 2 },
  { generatorKey: 'vegetableCorn', amountPerBaseQuantity: 1 },
  { generatorKey: 'vegetablePotato', amountPerBaseQuantity: 2 },
  { generatorKey: 'vegetableMushrooms', amountPerBaseQuantity: 1 }
];

const SEAFOOD_BREAKDOWN_BASE_GRAMS = 480;
const SEAFOOD_BREAKDOWN_RULES: BreakdownRule[] = [
  { generatorKey: 'seafoodShrimp', amountPerBaseQuantity: 1 },
  { generatorKey: 'seafoodScallop', amountPerBaseQuantity: 0.75 },
  { generatorKey: 'seafoodSquid', amountPerBaseQuantity: 0.75 }
];

const DEFAULT_BBQ_ANSWERS: BBQGenerationAnswers = {
  adultCount: 4,
  childCount: 0,
  appetite: 'normal',
  balance: 'balanced',
  drinkLevel: 'normal',
  alcoholLevel: 'none',
  alcoholAmount: 'normal',
  seafoodLevel: 'none',
  stapleLevel: 'light'
};

export const DEFAULT_BBQ_ADJUSTMENTS: BBQGenerationAdjustments = {
  meat: 'normal',
  seafood: 'normal',
  vegetables: 'normal',
  drinks: 'normal',
  staple: 'normal',
  overall: 'normal'
};

export function createDefaultBBQGenerationConfig(): BBQGenerationConfig {
  return {
    version: 1,
    answers: { ...DEFAULT_BBQ_ANSWERS },
    adjustments: { ...DEFAULT_BBQ_ADJUSTMENTS }
  };
}

export function generateBBQItems({ answers, adjustments }: GenerateBBQItemsInput): GeneratedItemCandidate[] {
  const people = Math.max(0, answers.adultCount) + Math.max(0, answers.childCount) * CHILD_TO_ADULT_FACTOR;
  if (people <= 0) {
    return [];
  }

  const appetiteFactor = APPETITE_FACTORS[answers.appetite];
  const overallFactor = LEVEL_FACTORS[adjustments.overall];
  const balanceFactors = BALANCE_FACTORS[answers.balance] ?? BALANCE_FACTORS.balanced;
  const meatFactor = LEVEL_FACTORS[adjustments.meat];
  const seafoodFactor = LEVEL_FACTORS[adjustments.seafood];
  const stapleAdjustmentFactor = LEVEL_FACTORS[adjustments.staple];
  const seafoodIncluded = answers.seafoodLevel === 'included';
  const seafoodMeatFactor = seafoodIncluded ? SEAFOOD_MEAT_REDUCTION_FACTOR : 1;
  const vegetableQuantity = roundTo(
    people *
      VEGETABLE_GRAMS_PER_PERSON *
      appetiteFactor *
      overallFactor *
      LEVEL_FACTORS[adjustments.vegetables] *
      balanceFactors.vegetables,
    50
  );

  const candidates: GeneratedItemCandidate[] = [
    candidate(
      'beef',
      roundTo(
        people *
          BEEF_GRAMS_PER_PERSON *
          appetiteFactor *
          overallFactor *
          meatFactor *
          balanceFactors.beef *
          seafoodMeatFactor,
        50
      )
    ),
    candidate(
      'pork',
      roundTo(
        people *
          PORK_GRAMS_PER_PERSON *
          appetiteFactor *
          overallFactor *
          meatFactor *
          balanceFactors.pork *
          seafoodMeatFactor,
        50
      )
    ),
    candidate(
      'chicken',
      roundTo(
        people *
          CHICKEN_GRAMS_PER_PERSON *
          appetiteFactor *
          overallFactor *
          meatFactor *
          balanceFactors.chicken *
          seafoodMeatFactor,
        50
      )
    ),
    candidate(
      'sausage',
      roundTo(
        people *
          SAUSAGE_GRAMS_PER_PERSON *
          appetiteFactor *
          overallFactor *
          meatFactor *
          balanceFactors.sausage *
          seafoodMeatFactor,
        50
      )
    ),
    ...expandBreakdown(VEGETABLE_BREAKDOWN_RULES, vegetableQuantity, VEGETABLE_BREAKDOWN_BASE_GRAMS),
    candidate(
      'softDrinks',
      roundTo(
        people *
          SOFT_DRINK_ML_PER_PERSON *
          LEVEL_FACTORS[answers.drinkLevel] *
          LEVEL_FACTORS[adjustments.drinks] *
          overallFactor,
        500
      )
    )
  ];

  if (seafoodIncluded) {
    const seafoodQuantity = roundTo(
      people * SEAFOOD_GRAMS_PER_PERSON * appetiteFactor * overallFactor * seafoodFactor,
      50
    );
    candidates.push(...expandBreakdown(SEAFOOD_BREAKDOWN_RULES, seafoodQuantity, SEAFOOD_BREAKDOWN_BASE_GRAMS));
  }

  const alcoholQuantity = calculateAlcoholQuantity(answers, adjustments, overallFactor);
  if (alcoholQuantity > 0) {
    candidates.push(candidate('alcohol', alcoholQuantity));
  }

  const yakisobaQuantity = calculateYakisobaQuantity(
    people,
    answers.stapleLevel,
    appetiteFactor,
    overallFactor,
    stapleAdjustmentFactor
  );
  if (yakisobaQuantity > 0) {
    candidates.push(candidate('yakisoba', yakisobaQuantity));
  }

  return candidates.filter((item) => item.quantity > 0);
}

export function isBBQGenerationConfig(value: unknown): value is BBQGenerationConfig {
  if (value == null || typeof value !== 'object') {
    return false;
  }
  const candidate = value as BBQGenerationConfig;
  return (
    candidate.version === 1 &&
    isBBQGenerationAnswers(candidate.answers) &&
    isBBQGenerationAdjustments(candidate.adjustments)
  );
}

function isBBQGenerationAnswers(value: unknown): value is BBQGenerationAnswers {
  if (value == null || typeof value !== 'object') {
    return false;
  }
  const candidate = value as BBQGenerationAnswers;
  return (
    isNonNegativeNumber(candidate.adultCount) &&
    isNonNegativeNumber(candidate.childCount) &&
    isLevel(candidate.appetite) &&
    isBalance(candidate.balance) &&
    isLevel(candidate.drinkLevel) &&
    isAlcoholLevel(candidate.alcoholLevel) &&
    isLevel(candidate.alcoholAmount) &&
    isSeafoodLevel(candidate.seafoodLevel) &&
    isStapleLevel(candidate.stapleLevel)
  );
}

function isBBQGenerationAdjustments(value: unknown): value is BBQGenerationAdjustments {
  if (value == null || typeof value !== 'object') {
    return false;
  }
  const candidate = value as BBQGenerationAdjustments;
  return (
    isLevel(candidate.meat) &&
    isLevel(candidate.seafood) &&
    isLevel(candidate.vegetables) &&
    isLevel(candidate.drinks) &&
    isLevel(candidate.staple) &&
    isLevel(candidate.overall)
  );
}

function candidate(generatorKey: BBQGeneratorKey, quantity: number): GeneratedItemCandidate {
  const rule = BBQ_GENERATION_RULES[generatorKey];
  return {
    generatorKey: rule.generatorKey,
    name: rule.label,
    category: rule.category,
    quantity,
    baseUnit: rule.baseUnit,
    displayUnit: resolveDisplayUnit(rule.displayUnit, rule.baseUnit, quantity)
  };
}

function resolveDisplayUnit(defaultDisplayUnit: UnitCode, baseUnit: BaseUnit, quantity: number): UnitCode {
  if (baseUnit === 'g' && quantity >= 1000) {
    return 'kg';
  }
  return defaultDisplayUnit;
}

function expandBreakdown(
  rules: BreakdownRule[],
  aggregateQuantity: number,
  baseQuantity: number
): GeneratedItemCandidate[] {
  const scale = aggregateQuantity / baseQuantity;
  return rules.map((rule) => candidate(rule.generatorKey, Math.max(1, Math.ceil(scale * rule.amountPerBaseQuantity))));
}

function calculateYakisobaQuantity(
  people: number,
  stapleLevel: BBQStapleLevel,
  appetiteFactor: number,
  overallFactor: number,
  stapleAdjustmentFactor: number
): number {
  if (stapleLevel === 'none') {
    return 0;
  }
  const stapleFactor = stapleLevel === 'light' ? 0.65 : 1;
  return Math.ceil(people * stapleFactor * appetiteFactor * overallFactor * stapleAdjustmentFactor);
}

function calculateAlcoholQuantity(
  answers: BBQGenerationAnswers,
  adjustments: BBQGenerationAdjustments,
  overallFactor: number
): number {
  if (answers.alcoholLevel !== 'included' || answers.adultCount <= 0) {
    return 0;
  }
  return Math.ceil(
    (answers.adultCount *
      ALCOHOL_ML_PER_ADULT *
      LEVEL_FACTORS[answers.alcoholAmount] *
      LEVEL_FACTORS[adjustments.drinks] *
      overallFactor) /
      ALCOHOL_CAN_ML
  );
}

function roundTo(value: number, unit: number): number {
  return Math.ceil(value / unit) * unit;
}

function isNonNegativeNumber(value: unknown): value is number {
  return typeof value === 'number' && Number.isFinite(value) && value >= 0;
}

function isLevel(value: unknown): value is BBQGenerationLevel {
  return value === 'small' || value === 'normal' || value === 'large';
}

function isBalance(value: unknown): value is BBQGenerationBalance {
  return value === 'vegetables' || value === 'balanced' || value === 'meat' || value === 'seafood';
}

function isStapleLevel(value: unknown): value is BBQStapleLevel {
  return value === 'none' || value === 'light' || value === 'solid';
}

function isAlcoholLevel(value: unknown): value is BBQAlcoholLevel {
  return value === 'none' || value === 'included';
}

function isSeafoodLevel(value: unknown): value is BBQSeafoodLevel {
  return value === 'none' || value === 'included';
}
