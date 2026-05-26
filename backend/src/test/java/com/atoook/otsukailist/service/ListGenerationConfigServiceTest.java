package com.atoook.otsukailist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.atoook.otsukailist.dto.ListGenerationConfigRequest;
import com.atoook.otsukailist.model.GenerationConfigType;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.model.ListGenerationConfig;
import com.atoook.otsukailist.repository.ItemListRepository;
import com.atoook.otsukailist.repository.ListGenerationConfigRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListGenerationConfigServiceTest {

  @Mock private ItemListRepository itemListRepo;
  @Mock private ListGenerationConfigRepository listGenerationConfigRepo;
  @Mock private ListRevisionService listRevisionService;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private ListGenerationConfigService service;

  @BeforeEach
  void setUp() {
    service =
        new ListGenerationConfigService(
            itemListRepo, listGenerationConfigRepo, listRevisionService, objectMapper);
  }

  @Test
  @DisplayName("生成設定をupsertしrevisionを返すこと")
  void upsertConfigStoresJsonAndReturnsRevision() throws Exception {
    UUID listId = UUID.randomUUID();
    ItemList list = new ItemList();
    list.setId(listId);
    list.setName("BBQ");
    var configJson = objectMapper.readTree("{\"version\":1,\"answers\":{\"adultCount\":6}}");
    ListGenerationConfigRequest request =
        ListGenerationConfigRequest.builder().configJson(configJson).build();

    when(itemListRepo.findById(listId)).thenReturn(Optional.of(list));
    when(listGenerationConfigRepo.findByItemListIdAndConfigType(listId, GenerationConfigType.BBQ))
        .thenReturn(Optional.empty());
    when(listGenerationConfigRepo.save(any(ListGenerationConfig.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(listRevisionService.incrementAndGet(listId)).thenReturn(3L);

    var result = service.upsertConfig(listId, "bbq", request);

    assertThat(result.getRevision()).isEqualTo(3L);
    assertThat(result.getData().getListId()).isEqualTo(listId);
    assertThat(result.getData().getConfigType()).isEqualTo(GenerationConfigType.BBQ);
    assertThat(result.getData().getConfigJson().get("version").asInt()).isEqualTo(1);
  }
}
