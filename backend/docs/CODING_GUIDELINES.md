# OtsukaiList Backend - コーディングガイドライン

## 目次

1. [🎯 設計思想](#-設計思想)
2. [🔧 Spring フレームワーク規約](#-spring-フレームワーク規約)
3. [⚡ 具体的実装指針](#-具体的実装指針)
4. [🧪 テスト・品質管理](#-テスト品質管理)

---

# 🎯 設計思想

## 基本原則

### セキュリティファースト

- **アクセス制御の徹底**: 全ての操作はリスト ID によるスコープ制限
- **情報漏洩の防止**: Entity を直接外部公開しない
- **権限分離**: URL パスとリクエストボディの責任分離

### シンプリシティ

- **UUID ベースの URL 共有**: ログイン不要でシンプルな共有機能
- **最小権限の原則**: 必要最小限の情報のみを公開
- **明確な責任分離**: 各層の役割を明確に定義

### 保守性・拡張性

- **疎結合設計**: 各層の独立性を保持
- **一貫性のあるパターン**: 統一されたコーディングスタイル
- **ドキュメント駆動**: 設計判断の根拠を明文化

## アーキテクチャ設計思想

### レイヤードアーキテクチャ

```
Controller → Service → Repository → Entity
     ↓         ↓          ↓         ↓
   DTO    ←  DTO    ←  Entity  ←  Database
```

### 情報フロー設計

- **外部 → 内部**: DTO で受け取り、Entity に変換
- **内部 → 外部**: Entity から DTO に変換して返却
- **セキュリティ境界**: Service 層でアクセス制御を実施

---

# 🔧 Spring フレームワーク規約

## 依存性注入（DI）パターン

### コンストラクタインジェクション（強く推奨）

```java
@Service
public class ItemService {
    private final ItemRepository itemRepository;           // final で不変
    private final ItemListRepository itemListRepository;

    // @Autowired は単一コンストラクタの場合省略可能（Spring 4.3+）
    public ItemService(ItemRepository itemRepository,
                      ItemListRepository itemListRepository) {
        this.itemRepository = itemRepository;
        this.itemListRepository = itemListRepository;
    }
}
```

### 使い分け指針

| パターン           | 推奨度 | 用途                           | 理由                                       |
| ------------------ | ------ | ------------------------------ | ------------------------------------------ |
| **コンストラクタ** | ⭐⭐⭐ | 必須依存関係（99%のケース）    | 不変性、テスタビリティ、循環依存の早期検出 |
| **セッター**       | ⚠️     | 任意依存関係（特殊ケースのみ） | `required = false` による柔軟性            |
| **フィールド**     | ❌     | 使用禁止                       | テスト困難、循環依存、可変性の問題         |

## Spring アノテーション規約

### REST Controller

```java
@RestController
@RequestMapping("/api/lists/{listId}/items")
@Validated
public class ItemController {

    @PostMapping
    @Transactional
    public ResponseEntity<ItemResponse> createItem(
            @PathVariable UUID listId,
            @Valid @RequestBody CreateItemRequest request) {
        // 実装
    }
}
```

### JPA Repository

```java
@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    List<Item> findByItemListId(UUID itemListId);
    Optional<Item> findByIdAndItemListId(UUID id, UUID itemListId);
    boolean existsByIdAndItemListId(UUID id, UUID itemListId);
}
```

### データベースアクセス規約

**✅ 推奨：Entity + Hibernate によるデータアクセス**

```java
@Entity
@Table(name = "item")
public class Item {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "list_id")
    private ItemList itemList;
}

// Repository での標準的な操作
@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    // Hibernate が自動生成するメソッド
    List<Item> findByItemListId(UUID itemListId);
    Optional<Item> findByIdAndItemListId(UUID itemId, UUID itemListId);

    // カスタムクエリ（JPQL使用）
    @Query("SELECT i FROM Item i WHERE i.itemList.id = :listId AND i.name LIKE %:name%")
    List<Item> findByListIdAndNameContaining(@Param("listId") UUID listId, @Param("name") String name);
}
```

**❌ 禁止：生 DML の直接実行**

```java
// ❌ 禁止パターン - 生SQL
@Query(value = "SELECT * FROM item WHERE list_id = ?1", nativeQuery = true)
List<Object[]> findItemsRaw(String listId);

// ❌ 禁止パターン - EntityManagerでの生SQL
entityManager.createNativeQuery("INSERT INTO item (id, name) VALUES (?, ?)")
            .setParameter(1, "item-123")
            .setParameter(2, "牛乳");

// ❌ 禁止パターン - @Modifying + @Query での更新/削除
@Modifying
@Query("UPDATE Item i SET i.name = :name WHERE i.id = :id")
int updateItemName(@Param("id") UUID id, @Param("name") String name);

@Modifying
@Query("DELETE FROM Item i WHERE i.itemList.id = :listId")
int deleteAllByListId(@Param("listId") UUID listId);
```

**理由：**

- **型安全性**: Entity によるコンパイル時チェック
- **保守性**: スキーマ変更時の自動対応
- **可読性**: ビジネスロジックと SQL の分離
- **移植性**: データベース非依存
- **`@Modifying`の問題**:
  - **1 次キャッシュ不整合**: DB 更新後も EntityManager の 1 次キャッシュが古いまま残り、同一トランザクション内で不整合が発生
  - **関連エンティティの整合性問題**: オブジェクトグラフの依存関係が更新されない
  - **Hibernate の自動機能無効化**: Dirty Checking、Cascade、楽観的ロック等が効かない

**⚠️ 例外：`@Modifying` が適切な場面**

```java
// ✅ 例外的に許可：ListRevision のように DB カウンタを原子的に更新するケース
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("update ItemList l set l.revision = l.revision + 1 where l.id = :listId")
int incrementRevision(@Param("listId") UUID listId);

// 使用時の注意事項：
// 1. clearAutomatically/flushAutomatically で 1 次キャッシュと同期を取る
// 2. 更新後に Repository 経由で再読込し、新しい revision を返却する
// 3. 例外は ListRevisionService など専用のユースケースだけに限定する
// 4. 必ず単体テストで整合性を確認
```

**✅ 推奨：Entity を使用した標準的な更新・削除**

```java
// ✅ 推奨パターン - Entityベースの操作
@Transactional
public Optional<MutationResponse<ItemResponse>> updateItem(UUID listId, UUID itemId, UpdateItemRequest request) {
    Optional<Item> existingOpt = this.itemRepository.findByIdAndItemListId(itemId, listId);

    if (existingOpt.isEmpty()) {
        return Optional.empty();
    }

    Item existing = existingOpt.get();
    ItemMapper.updateEntity(existing, request);  // 名前などの変更

    Item saved = this.itemRepository.save(existing);  // Hibernateが自動でUPDATE
    long revision = listRevisionService.incrementAndGet(listId);
    return Optional.of(MutationResponse.<ItemResponse>builder()
            .revision(revision)
            .data(ItemMapper.toResponse(saved))
            .build());
}
```

### トランザクション管理

```java
@Service
@Transactional(readOnly = true)  // クラスレベルで読み取り専用
public class ItemService {

    @Transactional  // 書き込み操作でオーバーライド
    public ItemResponse createItem(UUID listId, CreateItemRequest request) {
        // 実装
    }
}
```

---

# ⚡ 具体的実装指針

## セキュリティファースト設計

### 必須原則：リスト ID スコープ

**❌ 禁止パターン**

```java
// 全体検索は禁止（設計思想に反する）
List<Item> findAll();
Item findById(UUID itemId);
```

**✅ 推奨パターン**

```java
// 必ずリスト ID でスコープを限定
List<Item> findByItemListId(UUID listId);
Optional<Item> findByIdAndItemListId(UUID itemId, UUID listId);
boolean existsByIdAndItemListId(UUID itemId, UUID listId);
```

### セキュリティ認証の実装パターン

```java
@Transactional
public Optional<MutationResponse<ItemResponse>> updateItem(UUID listId, UUID itemId, UpdateItemRequest request) {
    // 1. 認証: listId + itemId での厳格チェック
    Optional<Item> existingOpt = this.itemRepository.findByIdAndItemListId(itemId, listId);

    if (existingOpt.isEmpty()) {
        return Optional.empty();  // アクセス権限なし
    }

    Item item = existingOpt.get();
    // 2. ビジネスロジック実行（名称更新 + 完了ロジック）
    ItemMapper.updateEntity(item, request);
    if (request.getCompleted() != null) {
        applyCompletionRules(item, request); // Service 内で完了者存在チェックや timestamp 設定
    }

    // 3. 保存と revision 生成
    Item saved = this.itemRepository.save(item);
    long revision = listRevisionService.incrementAndGet(listId);

    return Optional.of(MutationResponse.<ItemResponse>builder()
            .revision(revision)
            .data(ItemMapper.toResponse(saved))
            .build());
}
```

**実装のポイント:**

- **トランザクション統合**: 認証・更新・保存を単一トランザクションで実行
- **早期リターン**: 認証失敗時は即座に`Optional.empty()`で終了
- **Revision一貫性**: `ListRevisionService` を必ず経由し、REST/WebSocket 両方で同じ revision を利用
- **パフォーマンス**: 同一トランザクション内での`save()`は効率的

## DTO パターン設計

### 設計原則

- **リクエスト/レスポンス分離**: セキュリティと責任の明確化
- **Entity 隠蔽**: JPA Entity を直接外部公開しない
- **バリデーション統合**: DTO レベルでの入力検証
- **正規化処理は Service で担当**: `trim()` や重複チェックなどの業務的ルールは `ListCommandService` / `ItemCommandService` などで行い、Mapper は純粋に値コピーへ専念させる。

### ディレクトリ構成

```
dto/
├── CreateItemRequest.java      # 作成リクエスト（最小限の情報）
├── UpdateItemRequest.java      # 更新リクエスト（部分更新対応）
└── ItemResponse.java           # レスポンス（完全な情報）

mapper/
└── ItemMapper.java             # Entity ↔ DTO 変換
```

### 実装パターン

```java
// Request DTO - 入力検証
@Getter @Setter @Builder
public class CreateItemRequest {
    @NotBlank(message = "アイテム名は必須です")
    @Size(max = 200, message = "アイテム名は200文字以下にしてください")
    private String name;

    @Builder.Default
    private boolean completed = false;
}

// Response DTO - 公開情報のみ
@Getter @Setter @Builder
public class ItemResponse {
    private String id;
    private String name;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String listId;  // セキュリティ確認用
}
```

---

## 💾 Repository 設計

### 戻り値型の使い分け

| 操作タイプ     | 戻り値型      | 理由                   | 例                                |
| -------------- | ------------- | ---------------------- | --------------------------------- |
| **複数件検索** | `List<T>`     | 空リストは正常結果     | `findByItemListId()`              |
| **単一件検索** | `Optional<T>` | 見つからない場合がある | `findByIdAndItemListId()`         |
| **存在確認**   | `boolean`     | Yes/No の明確な判定    | `existsByIdAndItemListId()`       |
| **件数取得**   | `long`        | 数値として明確         | `countByItemListIdAndCompleted()` |

### 実装例

## Repository 設計原則

### 戻り値パターンの使い分け

```java
@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    // ✅ 正しい：リストは空でも正常
    List<Item> findByItemListId(UUID itemListId);

    // ✅ 正しい：単一アイテムは見つからない可能性
    Optional<Item> findByIdAndItemListId(UUID itemId, UUID itemListId);

    // ✅ 正しい：存在確認は boolean
    boolean existsByIdAndItemListId(UUID itemId, UUID itemListId);

    // ❌ 間違い：リストにOptionalは不要
    // Optional<List<Item>> findByItemListId(UUID itemListId);
}
```

### 戻り値選択基準

- `List<T>`: コレクション（空でも正常な状態）
- `Optional<T>`: 単一オブジェクト（未発見は異常状態）
- `boolean`: 存在確認（性能優位性）

### 正統的 ID タイプ：UUID

**設計方針**: 本プロジェクトでは、エンティティの ID タイプとして`UUID`を採用しています。

**採用理由**:

- **グローバル一意性**: 複数のサーバーやデータベース間での重複を避けられる
- **セキュリティ**: 連番による推測攻撃を防止
- **分散システム対応**: 将来的なスケーラビリティを考慮
- **Hibernate 統合**: `@UuidGenerator`による自動生成に最適化

**実装例**: 全てのリポジトリインターフェースとメソッドシグニチャで`UUID`を使用

## トランザクション管理パターン

### Service レイヤーでの統合

```java
@Service
@Transactional(readOnly = true)  // クラスレベルで読み取り専用
public class ItemService {

    @Transactional  // 書き込み操作でオーバーライド
    public Optional<MutationResponse<ItemResponse>> updateItem(UUID listId, UUID itemId, UpdateItemRequest request) {
        // 認証・取得・更新を単一トランザクションで実行
        Optional<Item> existingOpt = this.itemRepository.findByIdAndItemListId(itemId, listId);

        if (existingOpt.isEmpty()) {
            return Optional.empty();  // ロールバック
        }

        Item existing = existingOpt.get();
        ItemMapper.updateEntity(existing, request);

        // 同一トランザクション内での save は効率的
        Item saved = this.itemRepository.save(existing);
        long revision = listRevisionService.incrementAndGet(listId);
        return Optional.of(MutationResponse.<ItemResponse>builder()
                .revision(revision)
                .data(ItemMapper.toResponse(saved))
                .build());
    }
}
```

### トランザクション設計の利点

- **ACID プロパティ保証**: 原子性、一貫性、分離性、永続性
- **パフォーマンス向上**: DB アクセス回数削減
- **データ一貫性**: 競合状態の回避

## RESTful API 設計原則

### URL 設計パターン

```java
// ✅ 推奨：リソース指向
GET    /api/lists/{listId}/items          // アイテム一覧
GET    /api/lists/{listId}/items/{itemId} // 特定アイテム取得
POST   /api/lists/{listId}/items          // アイテム作成
PATCH  /api/lists/{listId}/items/{itemId} // アイテム更新
DELETE /api/lists/{listId}/items/{itemId} // アイテム削除

// ❌ 非推奨：ID をボディに含める
PUT /api/lists/{listId}/items
{
  "id": "item-123",        // URLで指定すべき
  "name": "更新後の名前"
}
```

### HTTP ステータス設計

| 操作       | 成功時         | 失敗時                         |
| ---------- | -------------- | ------------------------------ |
| **GET**    | 200 OK         | 404 Not Found                  |
| **POST**   | 201 Created    | 400 Bad Request                |
| **PATCH**  | 200 OK         | 404 Not Found, 400 Bad Request |
| **DELETE** | 204 No Content | 404 Not Found                  |

### MutationResponse / Revision

- 更新系 API は `MutationResponse<T>` を返却して `revision` と変更内容を1つにまとめる。
- `revision` は `item_list.revision` の数値で、`ListRevisionService.incrementAndGet(listId)` で更新してからレスポンスを組み立てる。
- WebSocket 通知も同じ `MutationResponse` を再利用し、クライアントは `revision` を比較して二重反映を避ける。

### Controller 実装パターン

```java
@RestController
@RequestMapping("/api/lists")
public class ItemController {

    @PatchMapping("/{listId}/items/{itemId}")
    public ResponseEntity<MutationResponse<ItemResponse>> updateItem(
            @PathVariable UUID listId,                   // URLパスから
            @PathVariable UUID itemId,                   // URLパスから
            @RequestBody @Valid UpdateItemRequest request  // ボディから
    ) {
        Optional<MutationResponse<ItemResponse>> updated = itemService.updateItem(listId, itemId, request);
        return updated.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
}
```

## Lombok 活用パターン

### 推奨アノテーション

```java
// DTO クラス
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateItemRequest {
    // フィールド定義
}

// Entity クラス
@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "items")
public class Item {
    // JPA アノテーション + フィールド定義
}
```

### @Builder パターン

```java
// ビルダーパターンによる可読性向上
CreateItemRequest request = CreateItemRequest.builder()
    .name("牛乳")
    .quantity(1)
    .build();

// デフォルト値の指定
@Builder.Default
```

### ⚠️ boolean 型フィールドの命名規則（重要）

**問題の背景**: Lombok と Jackson の命名規則の不整合により、JSON シリアライゼーションエラーが発生

#### ✅ JSON 名称と Java 名称の同期

```java
public class ItemResponse {
    private boolean completed;  // JSONでは completed、Lombokが isCompleted() を生成
}

// Lombokが生成するメソッド：
// - public boolean isCompleted() { ... }
// - public void setCompleted(boolean completed) { ... }

// JSON出力例：
// { "completed": true }
```

#### 命名ルールとベストプラクティス

```java
// ✅ フィールド名は述語的に（"is" を付けない）
private boolean completed;
private boolean active;
private Boolean completed; // ラッパー型は null 許容
```

#### プリミティブ vs ラッパー型の使い分け

```java
// DTOでの使い分け例
public class CreateItemRequest {
    @Builder.Default
    private boolean completed = false;    // 必須、デフォルト値あり
}

public class UpdateItemRequest {
    private Boolean completed;            // 任意更新（null = 更新しない）

    // Lombokが生成：getCompleted() / setCompleted(...)
    // 使用例：if (request.getCompleted() != null) { ... }
}
```

#### 実装時の統一性確保

```java
// Entity（データベース層）
@Entity
public class Item {
    @Column(name = "is_completed")  // DB物理名
    private boolean completed;      // Java論理名
}

// DTO（API層）
public class ItemResponse {
    private boolean completed;      // 同じ論理名で統一
}

// Mapper（変換層）
public static ItemResponse toResponse(Item entity) {
    return ItemResponse.builder()
            .completed(entity.isCompleted())
            .build();
}
```

#### トラブルシューティング

**症状**: JSON に boolean フィールドが含まれない、または null になる  
**原因**: フィールド名に `is` 接頭辞を付けてしまい、Lombok/Jackson が一致しない  
**解決**: フィールド名から `is` を除去し、`completed` などの形容詞にする

---

# 🧪 テスト・品質管理

## Service レイヤーテスト

### Mockito を使用したユニットテスト

```java
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private ItemListRepository itemListRepository;

    @InjectMocks private ItemService itemService;

    @Test
    void updateItem_ValidAccess_Success() {
        // Given
        UUID listId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Item mockItem = new Item();
        UpdateItemRequest request = UpdateItemRequest.builder()
            .name("更新されたアイテム")
            .build();

        when(itemRepository.findByIdAndItemListId(itemId, listId))
            .thenReturn(Optional.of(mockItem));

        // When
        Optional<ItemResponse> result = itemService.updateItem(listId, itemId, request);

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void updateItem_InvalidAccess_ReturnsEmpty() {
        // Given
        UUID listId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UpdateItemRequest request = UpdateItemRequest.builder()
            .name("更新されたアイテム")
            .build();

        when(itemRepository.findByIdAndItemListId(itemId, listId))
            .thenReturn(Optional.empty());

        // When
        Optional<ItemResponse> result = itemService.updateItem(listId, itemId, request);

        // Then
        assertThat(result).isEmpty();
    }
}
```

## 開発チェックリスト

### 新機能開発時

- [ ] リスト ID によるスコープ制限を実装
- [ ] DTO パターンでリクエスト/レスポンス分離
- [ ] @Transactional でトランザクション境界設定
- [ ] セキュリティチェックを Service レイヤーで実装
- [ ] RESTful な URL 設計
- [ ] 適切な HTTP ステータスコード返却
- [ ] バリデーション実装
- [ ] 単体テスト作成

### コードレビュー時

- [ ] Entity を直接 Controller で返していない
- [ ] findAll() などの全件検索を使用していない
- [ ] リスト系メソッドが Optional<List> になっていない
- [ ] コンストラクタインジェクションを使用している
- [ ] トランザクション境界が適切
- [ ] セキュリティチェックが漏れていない
- [ ] **Entity + Hibernate でのデータアクセスを使用している**
- [ ] **生 DML や nativeQuery を不必要に使用していない**
- [ ] **@Modifying + @Query が必要最小限の使用に留まっている（大量データ処理など）**
- [ ] **boolean フィールドが "is" プレフィックスなしで命名されている**
- [ ] **Lombok と Jackson の統合で JSON シリアライゼーションが正常動作する**

---

## 🔗 関連ドキュメント

- [📖 README.md](../README.md) - セットアップと実行方法

---

> **最終更新**: 2024 年 12 月 26 日  
> **作成者**: Backend Development Team  
> **レビュー**: 実装完了時に都度更新

- [プロジェクト企画書](../docs/otsukailist企画書.md)
- [設計書](../docs/otsukailist設計書.md)
- [データベース設計](../db/README.md)
- [API 仕様書](./API_SPECIFICATION.md) _(TODO)_

---

**更新日: 2025-11-16**  
**チーム: OtsukaiList Development Team**
