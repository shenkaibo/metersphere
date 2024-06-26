<template>
  <MsCard simple no-content-padding>
    <div class="h-full p-[16px]">
      <MsAdvanceFilter
        v-model:keyword="keyword"
        :search-placeholder="t('bugManagement.recycle.searchPlaceholder')"
        :filter-config-list="filterConfigList"
        :row-count="filterRowCount"
        @keyword-search="fetchData"
        @adv-search="handleAdvSearch"
        @refresh="handleAdvSearch"
      >
        <template #left>
          <div></div>
        </template>
      </MsAdvanceFilter>
      <MsBaseTable
        class="mt-[16px]"
        v-bind="propsRes"
        :action-config="tableAction"
        v-on="propsEvent"
        @batch-action="handleTableBatch"
      >
        <template #operation="{ record }">
          <div class="flex flex-row flex-nowrap">
            <MsButton class="!mr-0" @click="handleRecover(record)">{{ t('bugManagement.recycle.recover') }}</MsButton>
            <a-divider direction="vertical" />
            <MsButton class="!mr-0" @click="handleDelete(record)">{{
              t('bugManagement.recycle.permanentlyDelete')
            }}</MsButton>
          </div>
        </template>

        <template #deleteTimeColumn="{ record }">
          {{ dayjs(record.deleteTime).format('YYYY-MM-DD HH:mm:ss') || '-' }}
        </template>

        <template #createUserFilter="{ columnConfig }">
          <TableFilter
            v-model:visible="createUserFilterVisible"
            v-model:status-filters="createUserFilterValue"
            :title="(columnConfig.title as string)"
            :list="createUserFilterOptions"
            value-key="value"
            @search="searchData()"
          >
            <template #item="{ item }">
              {{ item.text }}
            </template>
          </TableFilter>
        </template>

        <template #updateUserFilter="{ columnConfig }">
          <TableFilter
            v-model:visible="updateUserFilterVisible"
            v-model:status-filters="updateUserFilterValue"
            :title="(columnConfig.title as string)"
            :list="updateUserFilterOptions"
            value-key="value"
            @search="searchData()"
          >
            <template #item="{ item }">
              {{ item.text }}
            </template>
          </TableFilter>
        </template>

        <template #deleteUserFilter="{ columnConfig }">
          <TableFilter
            v-model:visible="deleteUserFilterVisible"
            v-model:status-filters="deleteUserFilterValue"
            :title="(columnConfig.title as string)"
            :list="deleteUserFilterOptions"
            value-key="value"
            @search="searchData()"
          >
            <template #item="{ item }">
              {{ item.text }}
            </template>
          </TableFilter>
        </template>

        <template #handleUserFilter="{ columnConfig }">
          <TableFilter
            v-model:visible="handleUserFilterVisible"
            v-model:status-filters="handleUserFilterValue"
            :title="(columnConfig.title as string)"
            :list="handleUserFilterOptions"
            value-key="value"
            @search="searchData()"
          >
            <template #item="{ item }">
              {{ item.text }}
            </template>
          </TableFilter>
        </template>

        <template #statusFilter="{ columnConfig }">
          <TableFilter
            v-model:visible="statusFilterVisible"
            v-model:status-filters="statusFilterValue"
            :title="(columnConfig.title as string)"
            :list="statusFilterOptions"
            value-key="value"
            @search="searchData()"
          >
            <template #item="{ item }">
              {{ item.text }}
            </template>
          </TableFilter>
        </template>

        <template #severityFilter="{ columnConfig }">
          <TableFilter
            v-model:visible="severityFilterVisible"
            v-model:status-filters="severityFilterValue"
            :title="(columnConfig.title as string)"
            :list="severityFilterOptions"
            value-key="value"
            @search="searchData()"
          >
            <template #item="{ item }">
              {{ item.text }}
            </template>
          </TableFilter>
        </template>

        <template #empty> </template>
      </MsBaseTable>
    </div>
  </MsCard>
</template>

<script lang="ts" async setup>
  import { ref } from 'vue';
  import { Message, TableData } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { BackEndEnum, FilterFormItem, FilterResult, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import TableFilter from '@/views/case-management/caseManagementFeature/components/tableFilter.vue';

  import {
    deleteBatchByRecycle,
    deleteSingleByRecycle,
    getCustomFieldHeader,
    getCustomOptionHeader,
    getRecycleList,
    recoverBatchByRecycle,
    recoverSingleByRecycle,
  } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit, customFieldDataToTableData, customFieldToColumns } from '@/utils';

  import { BugEditCustomField, BugListItem, BugOptionItem } from '@/models/bug-management';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const { openDeleteModal } = useModal();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const projectId = computed(() => appStore.currentProjectId);
  const filterVisible = ref(false);
  const filterRowCount = ref(0);
  const keyword = ref('');
  // 自定义字段
  const customFields = ref<BugEditCustomField[]>([]);
  const filterConfigList = reactive<FilterFormItem[]>([
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      type: FilterType.INPUT,
      backendType: BackEndEnum.STRING,
    },
    {
      title: 'bugManagement.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
  ]);

  // 表头筛选
  const createUserFilterOptions = ref<BugOptionItem[]>([]);
  const createUserFilterVisible = ref(false);
  const createUserFilterValue = ref<string[]>([]);
  const updateUserFilterOptions = ref<BugOptionItem[]>([]);
  const updateUserFilterVisible = ref(false);
  const updateUserFilterValue = ref<string[]>([]);
  const handleUserFilterOptions = ref<BugOptionItem[]>([]);
  const handleUserFilterVisible = ref(false);
  const handleUserFilterValue = ref<string[]>([]);
  const deleteUserFilterOptions = ref<BugOptionItem[]>([]);
  const deleteUserFilterVisible = ref(false);
  const deleteUserFilterValue = ref<string[]>([]);
  const statusFilterOptions = ref<BugOptionItem[]>([]);
  const statusFilterVisible = ref(false);
  const statusFilterValue = ref<string[]>([]);
  const severityFilterOptions = ref<BugOptionItem[]>([]);
  const severityFilterVisible = ref(false);
  const severityFilterValue = ref<string[]>([]);
  const severityColumnId = ref('');
  const filterResult = ref<FilterResult>({ accordBelow: 'AND', combine: {} });
  const currentSelectParams = ref<BatchActionQueryParams>({ selectAll: false, currentSelectCount: 0 });

  const heightUsed = computed(() => 286 + (filterVisible.value ? 160 + (filterRowCount.value - 1) * 60 : 0));

  const columns: MsTableColumn = [
    {
      title: 'bugManagement.recycle.deleteTime',
      dataIndex: 'deleteTime',
      showDrag: true,
      width: 199,
      slotName: 'deleteTimeColumn',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.recycle.deleteMan',
      dataIndex: 'deleteUserName',
      width: 125,
      showDrag: true,
      slotName: 'deleteUser',
      showTooltip: true,
      titleSlotName: 'deleteUserFilter',
      showInTable: true,
    },
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      width: 100,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      width: 250,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUser',
      slotName: 'handleUser',
      showTooltip: true,
      titleSlotName: 'handleUserFilter',
      width: 125,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'statusName',
      width: 100,
      slotName: 'status',
      titleSlotName: 'statusFilter',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'bugManagement.tag',
      showDrag: true,
      width: 456,
      isStringTag: true,
      dataIndex: 'tags',
    },
    {
      title: 'bugManagement.creator',
      dataIndex: 'createUser',
      slotName: 'createUser',
      width: 125,
      showTooltip: true,
      showDrag: true,
      titleSlotName: 'createUserFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.createTime',
      dataIndex: 'createTime',
      showDrag: true,
      width: 199,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.updateUser',
      dataIndex: 'updateUser',
      width: 125,
      showTooltip: true,
      showDrag: true,
      titleSlotName: 'updateUserFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.updateTime',
      dataIndex: 'updateTime',
      showDrag: true,
      width: 199,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 150,
    },
  ];

  // 获取自定义字段
  const getCustomFieldColumns = async () => {
    const res = await getCustomFieldHeader(projectId.value);
    customFields.value = res;

    //  实例化自定义字段的filters
    customFields.value.forEach((item) => {
      if ((item.fieldName === '严重程度' || item.fieldName === 'Bug Degree') && item.options) {
        severityFilterOptions.value = [];
        severityColumnId.value = `custom_single_${item.fieldId}`;
        item.options.forEach((option) => {
          severityFilterOptions.value.push({
            value: option.value,
            text: option.text,
          });
        });
      }
    });

    return customFieldToColumns(res);
  };

  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams, setProps } = useTable(
    getRecycleList,
    {
      tableKey: TableKeyEnum.BUG_MANAGEMENT_RECYCLE,
      selectable: true,
      noDisable: true,
      showSetting: true,
      scroll: { x: '1900px' },
      heightUsed: 256,
    },
    (record: TableData) => ({
      ...record,
      handleUser: record.handleUserName,
      createUser: record.createUserName,
      updateUser: record.updateUserName,
      ...customFieldDataToTableData(record.customFields, customFields.value),
    })
  );

  const tableAction = {
    baseAction: [
      {
        eventTag: 'recover',
        label: t('bugManagement.recycle.recover'),
        permission: ['PROJECT_BUG:READ+UPDATE'],
      },
      {
        eventTag: 'delete',
        label: t('bugManagement.recycle.permanentlyDelete'),
        permission: ['PROJECT_BUG:READ+DELETE'],
      },
    ],
  };

  watchEffect(() => {
    setProps({ heightUsed: heightUsed.value });
  });

  function initTableParams() {
    const filterParams: Record<string, any> = {
      status: statusFilterValue.value,
      handleUser: handleUserFilterValue.value,
      updateUser: updateUserFilterValue.value,
      createUser: createUserFilterValue.value,
      deleteUser: deleteUserFilterValue.value,
    };
    filterParams[severityColumnId.value] = severityFilterValue.value;
    return {
      keyword: keyword.value,
      projectId: projectId.value,
      filter: { ...filterParams },
      condition: {
        keyword: keyword.value,
        filter: propsRes.value.filter,
      },
    };
  }

  function searchData() {
    setLoadListParams(initTableParams());
    loadList();
  }

  const fetchData = async (v = '') => {
    setKeyword(v);
    searchData();
  };

  const handleAdvSearch = (filter: FilterResult) => {
    filterResult.value = filter;
    fetchData();
  };

  // 单个恢复
  const handleRecover = async (record: BugListItem) => {
    try {
      await recoverSingleByRecycle(record.id);
      Message.success(t('bugManagement.recycle.recoverSuccess'));
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 批量恢复
  const handleBatchRecover = async () => {
    try {
      appStore.showLoading(t('bugManagement.recycle.recovering'));
      const tmpObj = { ...currentSelectParams.value, projectId: projectId.value };
      await recoverBatchByRecycle(tmpObj);
      appStore.hideLoading();
      Message.success(t('bugManagement.recycle.recoverSuccess'));
      keyword.value = '';
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 单个删除
  const handleDelete = (record: BugListItem) => {
    openDeleteModal({
      title: t('bugManagement.recycle.permanentlyDeleteTip', { name: characterLimit(record.title) }),
      content: t('bugManagement.recycle.deleteContent'),
      onBeforeOk: async () => {
        try {
          await deleteSingleByRecycle(record.id);
          Message.success(t('common.deleteSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  };
  // 批量删除
  const handleBatchDelete = () => {
    openDeleteModal({
      title: t('bugManagement.recycle.batchDelete', { count: currentSelectParams.value.currentSelectCount }),
      content: t('bugManagement.recycle.deleteContent'),
      onBeforeOk: async () => {
        try {
          const tmpObj = { ...currentSelectParams.value, projectId: projectId.value };
          await deleteBatchByRecycle(tmpObj);
          Message.success(t('common.deleteSuccess'));
          keyword.value = '';
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  };

  // 处理表格选中后批量操作
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    const filterParams: Record<string, any> = {
      status: statusFilterValue.value,
      handleUser: handleUserFilterValue.value,
      updateUser: updateUserFilterValue.value,
      createUser: createUserFilterValue.value,
      deleteUser: deleteUserFilterValue.value,
    };
    filterParams[severityColumnId.value] = severityFilterValue.value;
    if (params.condition) {
      params.condition.filter = { ...filterParams };
    } else {
      params.condition = { filter: { ...filterParams } };
    }
    const condition = {
      keyword: keyword.value,
      searchMode: filterResult.value.accordBelow,
      filter: params.condition.filter,
      combine: filterResult.value.combine,
    };
    currentSelectParams.value = {
      excludeIds: params.excludeIds,
      selectAll: params.selectAll,
      selectIds: params.selectedIds,
      currentSelectCount: params.currentSelectCount,
      condition,
    };

    switch (event.eventTag) {
      case 'recover':
        handleBatchRecover();
        break;
      case 'delete':
        handleBatchDelete();
        break;
      default:
        break;
    }
  }

  async function initFilterOptions() {
    const res = await getCustomOptionHeader(appStore.currentProjectId);
    createUserFilterOptions.value = res.userOption;
    updateUserFilterOptions.value = res.userOption;
    deleteUserFilterOptions.value = res.userOption;
    handleUserFilterOptions.value = res.handleUserOption;
    statusFilterOptions.value = res.statusOption;
  }

  let customColumns: MsTableColumn = [];

  async function getColumnHeaders() {
    try {
      customColumns = await getCustomFieldColumns();
      customColumns.forEach((item) => {
        if (item.title === '严重程度' || item.title === 'Bug Degree') {
          item.showInTable = true;
          item.titleSlotName = 'severityFilter';
          item.slotName = 'severity';
        } else {
          item.showInTable = false;
        }
      });
    } catch (error) {
      console.log(error);
    }
  }
  await getColumnHeaders();

  await tableStore.initColumn(TableKeyEnum.BUG_MANAGEMENT_RECYCLE, columns.concat(customColumns), 'drawer');

  onMounted(() => {
    setLoadListParams({ projectId: projectId.value });
    initFilterOptions();
    fetchData();
  });
</script>

<style lang="less" scoped>
  .ms-table--special-small();
</style>
