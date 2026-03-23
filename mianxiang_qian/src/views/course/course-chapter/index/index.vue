<template>
  <div class="app-container course-detail-container">
    <el-row :gutter="20" class="course-detail-layout">
      <!-- 左侧：课程列表 -->
      <el-col :span="4" class="left-panel">
        <div class="panel-header">
          <h3>课程列表</h3>
          <el-input
            v-model="courseSearchKeyword"
            placeholder="搜索课程名称"
            prefix-icon="el-icon-search"
            clearable
            size="small"
            style="width: 100%; margin-top: 10px;"
            @input="handleCourseSearch"
          />
        </div>
        <div class="course-list-wrapper">
          <div
            v-for="course in filteredCourseList"
            :key="course.id"
            :class="['course-card', { active: selectedCourseId === course.id }]"
            @click="selectCourse(course)"
          >
            <div class="course-card-header">
              <image-preview
                v-if="course.cover"
                :src="course.cover"
                width="80px"
                height="60px"
                class="course-cover"
              />
              <div v-else class="course-cover-placeholder">
                <i class="el-icon-picture"></i>
              </div>
              <div class="course-info">
                <div class="course-name">{{ course.courseName }}</div>
                <div class="course-meta">
                  <el-tag
                    :type="course.difficultyLevel === 1 ? 'info' : course.difficultyLevel === 2 ? 'warning' : 'danger'"
                    size="small"
                  >
                    {{ getEnumLabel(course.difficultyLevel, difficultyLevelEnumMap) }}
                  </el-tag>
                </div>
              </div>
            </div>
            <div class="course-card-footer">
              <span class="chapter-count">
                <i class="el-icon-menu"></i>
                章节数：{{ course.chapterCount || 0 }}
              </span>
              <span class="teacher-name">
                <i class="el-icon-user"></i>
                {{ course.realName || '未知' }}
              </span>
            </div>
          </div>
          <div v-if="filteredCourseList.length === 0" class="empty-state">
            <i class="el-icon-document"></i>
            <p>暂无课程数据</p>
          </div>
        </div>
        <!-- 课程列表分页 -->
        <div class="course-pagination">
          <el-pagination
            :current-page.sync="courseQueryParams.current"
            :page-size.sync="courseQueryParams.size"
            :page-sizes="[5, 10, 20]"
            :total="courseTotal"
            layout="total, prev, pager, next"
            small
            @size-change="handleCourseSizeChange"
            @current-change="handleCourseCurrentChange"
          />
        </div>
      </el-col>

      <!-- 右侧：章节信息 -->
      <el-col :span="20" class="right-panel">
        <!-- 提示信息 -->
        <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template slot="default">
            <div style="line-height: 1.8;">
              <p style="margin: 0;">本页面仅显示草稿状态的课程，用于编辑课程详情和章节信息。审核通过后的课程不支持编辑课程详情，如需编辑请先在课程列表中点击"重新编辑"按钮将课程状态改为草稿。</p>
            </div>
          </template>
        </el-alert>
        
        <div v-if="selectedCourseId" class="chapter-panel">
          <div class="panel-header">
            <h3>
              <span v-if="selectedCourse">{{ selectedCourse.courseName }}</span>
              <span v-else>课程章节</span>
            </h3>
            <el-button type="primary" size="small" @click="handleAddChapter">
              <i class="el-icon-plus"></i> 新建章节
            </el-button>
          </div>

          <!-- 章节搜索 -->
          <div class="chapter-search">
            <el-form :inline="true" :model="chapterQueryParams" size="small">
        <el-form-item label="章节名称">
          <el-input
                  v-model="chapterQueryParams.chapterName"
            placeholder="请输入章节名称"
            clearable
                  @keyup.enter.native="handleChapterQuery"
          />
        </el-form-item>
        <el-form-item label="章节类型">
          <el-select
                  v-model="chapterQueryParams.chapterType"
            placeholder="请选择章节类型"
            clearable
          >
            <el-option
              :key="1"
              :label="'基础章节'"
              :value="1"
            />
            <el-option
              :key="2"
              :label="'难度章节'"
              :value="2"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="章节难度">
          <el-select
                  v-model="chapterQueryParams.difficultyLevel"
            placeholder="请选择章节难度"
            clearable
          >
            <el-option
              :key="1"
              :label="'简单'"
              :value="1"
            />
            <el-option
              :key="2"
              :label="'中等'"
              :value="2"
            />
            <el-option
              :key="3"
              :label="'困难'"
              :value="3"
            />
          </el-select>
        </el-form-item>
              <el-form-item>
                <el-button @click="handleChapterQuery">查询</el-button>
                <el-button @click="resetChapterQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 章节表格 -->
          <div class="chapter-table-wrapper">
            <el-table
              v-loading="chapterLoading"
              :data="chapterList"
              border
              style="width: 100%"
              :row-key="getChapterRowKey"
              :expand-row-keys="expandedChapterRows"
              @expand-change="handleChapterExpandChange"
            >
              <el-table-column type="expand">
                <template slot-scope="scope">
                  <div class="exam-paper-expand-content">
                    <div class="expand-indicator">
                      <i class="el-icon-arrow-right"></i>
                      <span class="expand-label">章节试卷</span>
                    </div>
                    <div class="expand-header">
                      <div class="expand-title">
                        <i class="el-icon-document"></i>
                        <span>试卷列表</span>
                        <el-tag size="mini" type="info" style="margin-left: 8px;">
                          共 {{ (scope.row.examPaperList || []).length }} 份
                        </el-tag>
                      </div>
                      <el-button
                        type="primary"
                        size="small"
                        icon="el-icon-plus"
                        @click="handleAddExamPaper(scope.row)"
                      >新建试卷</el-button>
                    </div>
                    <div class="expand-table-wrapper">
                      <el-table
                        v-loading="scope.row.examPaperLoading"
                        :data="scope.row.examPaperList || []"
                        border
                        size="small"
                        class="nested-table"
                      >
                      <el-table-column type="index" label="序号" width="60" align="center" />
                      <el-table-column label="试卷编号" prop="paperCode" align="center" width="120" />
                      <el-table-column label="试卷名称" prop="paperName" align="center" :show-overflow-tooltip="true" min-width="200" />
                      <el-table-column label="试卷难度" prop="difficultyLevel" align="center" width="100">
                        <template slot-scope="paperScope">
                          <el-tag :type="paperScope.row.difficultyLevel === 1 ? 'success' : paperScope.row.difficultyLevel === 2 ? 'warning' : 'danger'">
                            {{ getEnumLabel(paperScope.row.difficultyLevel, difficultyLevelEnumMap) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="题目数量" prop="questionCount" align="center" width="100" />
                      <el-table-column label="总分" prop="totalScore" align="center" width="100" />
                      <el-table-column label="考试时长(分钟)" prop="durationMinutes" align="center" width="120" />
                      <el-table-column label="生成方式" prop="generateType" align="center" width="100">
                        <template slot-scope="paperScope">
                          <el-tag :type="paperScope.row.generateType === 1 ? 'primary' : 'info'">
                            {{ getEnumLabel(paperScope.row.generateType, generateTypeEnumMap) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="试卷状态" prop="status" align="center" width="100">
                        <template slot-scope="paperScope">
                          <el-tag :type="paperScope.row.status === 1 ? 'success' : 'danger'">
                            {{ getEnumLabel(paperScope.row.status, paperStatusEnumMap) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" align="center" width="320" fixed="right">
                        <template slot-scope="paperScope">
                          <el-button
                            v-if="paperScope.row.status === 0"
                            type="text"
                            size="small"
                            style="color: #409EFF;"
                            @click="handlePaperCompose(paperScope.row, scope.row)"
                          >组卷</el-button>
                          <el-button
                            v-if="paperScope.row.status === 0"
                            type="text"
                            size="small"
                            @click="handleUpdateExamPaper(paperScope.row)"
                          >修改</el-button>
                          <el-button
                            v-if="paperScope.row.status === 1"
                            type="text"
                            size="small"
                            style="color: #67C23A;"
                            @click="handleDisableExamPaper(paperScope.row, scope.row)"
                          >停用</el-button>
                          <el-button
                            v-if="paperScope.row.status === 0"
                            type="text"
                            size="small"
                            style="color: #67C23A;"
                            @click="handleEnableExamPaper(paperScope.row, scope.row)"
                          >启用</el-button>
                          <el-button
                            type="text"
                            size="small"
                            style="color: #f56c6c;"
                            :disabled="paperScope.row.status === 1"
                            @click="handleDeleteExamPaper(paperScope.row, scope.row)"
                          >删除</el-button>
                        </template>
                      </el-table-column>
                      </el-table>
                      <div v-if="!scope.row.examPaperLoading && (!scope.row.examPaperList || scope.row.examPaperList.length === 0)" class="empty-paper-list">
                        <i class="el-icon-document-delete"></i>
                        <p>暂无试卷，点击"新建试卷"添加</p>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="章节名称" prop="chapterName" align="center" :show-overflow-tooltip="true" />
              <el-table-column label="章节类型" prop="chapterType" align="center" width="120">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.chapterType === 1 ? 'success' : 'warning'">
                    {{ getEnumLabel(scope.row.chapterType, chapterTypeEnumMap) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="章节难度" prop="difficultyLevel" align="center" width="100">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.difficultyLevel === 1 ? 'info' : scope.row.difficultyLevel === 2 ? 'warning' : 'danger'">
                    {{ getEnumLabel(scope.row.difficultyLevel, difficultyLevelEnumMap) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="章节排序" prop="chapterOrder" align="center" width="100" />
              <el-table-column label="课件数量" prop="contentCount" align="center" width="100" />
              <el-table-column label="测试题数量" prop="questionCount" align="center" width="120" />
              <el-table-column label="试卷数量" prop="examPaperCount" align="center" width="100" />
              <el-table-column label="操作" align="center" width="340" fixed="right">
                <template slot-scope="scope">
                  <el-button
                    type="text"
                    size="small"
                    style="color: #67C23A;"
                    @click="handleContentManage(scope.row)"
                  >内容维护</el-button>
                  <el-button
                    type="text"
                    size="small"
                    style="color: #409EFF;"
                    @click="handleQuestionManage(scope.row)"
                  >章节测试题</el-button>
                  <el-button
                    type="text"
                    size="small"
                    @click="handleUpdateChapter(scope.row)"
                  >修改</el-button>
                  <el-button
                    type="text"
                    size="small"
                    style="color: #f56c6c;"
                    @click="handleDeleteChapter(scope.row)"
                  >删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 章节分页 -->
          <div class="chapter-pagination">
            <el-pagination
              :current-page.sync="chapterQueryParams.current"
              :page-size.sync="chapterQueryParams.size"
              :page-sizes="[5, 10, 20, 50]"
              :total="chapterTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleChapterSizeChange"
              @current-change="handleChapterCurrentChange"
            />
          </div>
        </div>

        <!-- 未选择课程时的提示 -->
        <div v-else class="empty-selection">
          <i class="el-icon-info"></i>
          <p>请从左侧选择课程查看章节信息</p>
        </div>
      </el-col>
    </el-row>

    <!-- 内容维护对话框 -->
    <el-dialog title="章节内容维护" :visible.sync="contentManageOpen" width="40%" append-to-body class="content-manage-dialog">
      <div v-if="currentChapter" class="content-manage-container">
        <div class="content-header">
          <h3>章节：{{ currentChapter.chapterName }}</h3>
          <el-button type="primary" size="small" @click="handleAddContent">
            <i class="el-icon-plus"></i> 新建内容
          </el-button>
        </div>
        <el-table
          v-loading="contentLoading"
          :data="contentList"
          border
          style="width: 100%; margin-top: 20px;"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="内容标题" prop="contentTitle" align="center" :show-overflow-tooltip="true" />
          <el-table-column label="内容类型" prop="contentType" align="center" width="120">
            <template slot-scope="scope">
              <el-tag :type="scope.row.contentType === 1 ? 'success' : 'info'">
                {{ getEnumLabel(scope.row.contentType, contentTypeEnumMap) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="视频" prop="videoUrl" align="center" width="120">
            <template slot-scope="scope">
              <el-button
                v-if="scope.row.videoUrl"
                type="text"
                size="small"
                @click="handleViewVideo(scope.row.videoUrl)"
              >
                <i class="el-icon-video-camera"></i> 查看视频
              </el-button>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="文档" prop="docUrl" align="center" width="120">
            <template slot-scope="scope">
              <el-button
                v-if="scope.row.docUrl"
                type="text"
                size="small"
                @click="handleViewPdf(scope.row.docUrl)"
              >
                <i class="el-icon-document"></i> 查看文档
              </el-button>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="封面" align="center" width="90">
            <template slot-scope="scope">
              <img
                v-if="scope.row.coverImage"
                :src="getFullImageUrl(scope.row.coverImage)"
                class="content-cover-thumb"
                alt="封面"
              >
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="排序" prop="sortOrder" align="center" width="80" />
          <el-table-column label="操作" align="center" width="180" fixed="right">
            <template slot-scope="scope">
              <el-button
                type="text"
                size="small"
                @click="handleUpdateContent(scope.row)"
              >修改</el-button>
              <el-button
                type="text"
                size="small"
                style="color: #f56c6c;"
                @click="handleDeleteContent(scope.row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 添加或修改内容对话框 -->
    <el-dialog :title="contentTitle" :visible.sync="contentDialogOpen" width="600px" append-to-body>
      <el-form ref="contentForm" :model="contentForm" :rules="contentRules" label-width="100px">
        <el-form-item label="内容标题" prop="contentTitle">
          <el-input v-model="contentForm.contentTitle" placeholder="请输入内容标题" />
        </el-form-item>
        <el-form-item label="内容类型" prop="contentType">
          <el-radio-group v-model="contentForm.contentType" @change="handleContentTypeChange">
            <el-radio
              v-for="(item, index) in contentTypeOptions"
              :key="index"
              :label="item.value"
            >{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 学习视频：显示视频上传组件 -->
        <el-form-item v-if="contentForm.contentType === 1" label="视频文件" prop="videoUrl">
          <video-upload v-model="contentForm.videoUrl" :file-size="500" />
        </el-form-item>
        <!-- 学习文档：显示PDF上传组件 -->
        <el-form-item v-if="contentForm.contentType === 2" label="文档文件" prop="docUrl">
          <pdf-upload v-model="contentForm.docUrl" :limit="1" :file-size="50" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <img-upload v-model="contentForm.coverImage" :limit="1" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input v-model.number="contentForm.sortOrder" type="number" placeholder="请输入排序" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitContentForm">确 定</el-button>
        <el-button @click="cancelContentForm">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 视频查看对话框 -->
    <el-dialog
      title="视频播放"
      :visible.sync="videoViewOpen"
      width="40%"
      append-to-body
      class="video-view-dialog"
    >
      <video-player v-if="currentVideoUrl" :src="currentVideoUrl" width="100%" height="500px" />
    </el-dialog>

    <!-- PDF查看对话框 -->
    <el-dialog
      title="文档预览"
      :visible.sync="pdfViewOpen"
      width="40%"
      append-to-body
      class="pdf-view-dialog"
      :close-on-click-modal="false"
      @close="handlePdfDialogClose"
    >
      <pdf-viewer
        v-if="currentPdfUrl"
        :key="`pdf-${pdfViewKey}-${currentPdfUrl}`"
        :src="currentPdfUrl"
        style="height: 70vh;"
      />
    </el-dialog>

    <!-- 章节测试题管理对话框 -->
    <el-dialog title="章节测试题管理" :visible.sync="questionManageOpen" width="50%" append-to-body class="question-manage-dialog">
      <div v-if="currentQuestionChapter" class="question-manage-container">
        <div class="question-header">
          <h3>章节：{{ currentQuestionChapter.chapterName }}</h3>
          <el-button type="primary" size="small" @click="handleAddQuestion">
            <i class="el-icon-plus"></i> 新建题目
          </el-button>
        </div>

        <!-- 搜索表单 -->
        <div class="question-search">
          <el-form :inline="true" :model="questionQueryParams" size="small">
            <el-form-item label="题目标题">
          <el-input
                v-model="questionQueryParams.questionTitle"
                placeholder="请输入题目标题"
            clearable
                @keyup.enter.native="handleQuestionQuery"
          />
        </el-form-item>
            <el-form-item label="难度等级">
              <el-select
                v-model="questionQueryParams.difficultyLevel"
                placeholder="请选择难度等级"
                clearable
              >
                <el-option
                  :key="1"
                  :label="'低'"
                  :value="1"
                />
                <el-option
                  :key="2"
                  :label="'中'"
                  :value="2"
                />
                <el-option
                  :key="3"
                  :label="'高'"
                  :value="3"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button @click="handleQuestionQuery">查询</el-button>
              <el-button @click="resetQuestionQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

        <!-- Tab标签页 -->
        <el-tabs v-model="questionActiveTab" @tab-click="handleQuestionTabClick" class="question-tabs">
          <el-tab-pane label="单选题" name="single">
            <span slot="label">
              <i class="el-icon-circle-check"></i> 单选题
            </span>
          </el-tab-pane>
          <el-tab-pane label="多选题" name="multiple">
            <span slot="label">
              <i class="el-icon-check"></i> 多选题
            </span>
          </el-tab-pane>
          <el-tab-pane label="判断题" name="judge">
            <span slot="label">
              <i class="el-icon-success"></i> 判断题
            </span>
          </el-tab-pane>
          <el-tab-pane label="填空题" name="fill">
            <span slot="label">
              <i class="el-icon-edit"></i> 填空题
            </span>
          </el-tab-pane>
          <el-tab-pane label="主观题" name="subjective">
            <span slot="label">
              <i class="el-icon-document"></i> 主观题
            </span>
          </el-tab-pane>
        </el-tabs>

        <!-- 题目表格 -->
      <el-table
          v-loading="questionLoading"
          :data="questionList"
          border
          style="width: 100%; margin-top: 20px;"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="题目标题" prop="questionTitle" align="center" :show-overflow-tooltip="true" min-width="200" />
          <el-table-column label="难度等级" prop="difficultyLevel" align="center" width="100">
        <template slot-scope="scope">
              <el-tag :type="scope.row.difficultyLevel === 1 ? 'success' : scope.row.difficultyLevel === 2 ? 'warning' : 'danger'">
                {{ getEnumLabel(scope.row.difficultyLevel, questionDifficultyEnumMap) }}
              </el-tag>
        </template>
      </el-table-column>
          <el-table-column label="答案内容" prop="answerContent" align="center" :show-overflow-tooltip="true" min-width="150" />
          <el-table-column label="解析" prop="analysisContent" align="center" :show-overflow-tooltip="true" min-width="150" />
          <el-table-column label="题目分值" prop="score" align="center" width="100" />
          <el-table-column label="题目状态" prop="status" align="center" width="100">
        <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                {{ getEnumLabel(scope.row.status, questionStatusEnumMap) }}
              </el-tag>
        </template>
      </el-table-column>
          <el-table-column label="操作" align="center" width="180" fixed="right">
        <template slot-scope="scope">
          <el-button
            type="text"
                size="small"
                @click="handleUpdateQuestion(scope.row)"
          >修改</el-button>
          <el-button
            type="text"
                size="small"
            style="color: #f56c6c;"
                @click="handleDeleteQuestion(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>
    </el-dialog>

    <!-- 添加或修改题目对话框 -->
    <el-dialog :title="questionTitle" :visible.sync="questionDialogOpen" width="700px" append-to-body>
      <el-form ref="questionForm" :model="questionForm" :rules="questionRules" label-width="100px">
        <el-form-item label="题目标题" prop="questionTitle">
          <el-input v-model="questionForm.questionTitle" placeholder="请输入题目标题" />
        </el-form-item>
        <el-form-item label="题目类型" prop="questionType">
          <el-select v-model="questionForm.questionType" placeholder="请选择题目类型" clearable style="width: 100%;" :disabled="questionForm.id != null" @change="handleQuestionTypeChange">
            <el-option
              v-for="(item, index) in questionTypeOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级" prop="difficultyLevel">
          <el-select v-model="questionForm.difficultyLevel" placeholder="请选择难度等级" clearable style="width: 100%;">
            <el-option
              v-for="(item, index) in questionDifficultyOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <!-- 选项管理（仅针对单选题、多选题、填空题） -->
        <el-form-item
          v-if="questionForm.questionType === 1 || questionForm.questionType === 2 || questionForm.questionType === 4"
          label="题目选项"
          prop="questionOptions"
        >
          <div class="question-options-container">
            <div
              v-for="(option, index) in questionForm.questionOptions"
              :key="index"
              class="question-option-item"
            >
              <div class="option-header">
                <span class="option-label">{{ getOptionLabel(index) }}</span>
                <el-checkbox
                  v-if="questionForm.questionType === 1 || questionForm.questionType === 2"
                  v-model="option.isCorrect"
                  :true-label="1"
                  :false-label="0"
                >正确答案</el-checkbox>
                <el-button
                  type="text"
                  size="small"
                  style="color: #f56c6c;"
                  @click="removeQuestionOption(index)"
                >删除</el-button>
              </div>
              <el-input
                v-model="option.optionContent"
                type="textarea"
                :rows="2"
                :placeholder="questionForm.questionType === 4 ? '请输入答案' : '请输入选项内容'"
              />
            </div>
            <el-button
              type="text"
              size="small"
              icon="el-icon-plus"
              @click="addQuestionOption"
            >添加选项</el-button>
          </div>
        </el-form-item>
        <el-form-item
          v-else
          label="答案内容"
          prop="answerContent"
        >
          <el-input
            v-model="questionForm.answerContent"
            type="textarea"
            :rows="3"
            placeholder="请输入答案内容"
          />
        </el-form-item>
        <el-form-item label="解析" prop="analysisContent">
          <el-input
            v-model="questionForm.analysisContent"
            type="textarea"
            :rows="3"
            placeholder="请输入解析"
          />
        </el-form-item>
        <el-form-item label="题目分值" prop="score">
          <el-input v-model.number="questionForm.score" type="number" placeholder="请输入题目分值" />
        </el-form-item>
        <el-form-item label="题目状态" prop="status">
          <el-radio-group v-model="questionForm.status">
            <el-radio
              v-for="(item, index) in questionStatusOptions"
              :key="index"
              :label="item.value"
            >{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitQuestionForm">确 定</el-button>
        <el-button @click="cancelQuestionForm">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 添加或修改章节对话框 -->
    <el-dialog :title="chapterTitle" :visible.sync="chapterDialogOpen" width="600px" append-to-body>
      <el-form ref="chapterForm" :model="chapterForm" :rules="chapterRules" label-width="100px">
        <el-form-item label="章节名称" prop="chapterName">
          <el-input v-model="chapterForm.chapterName" placeholder="请输入章节名称" />
        </el-form-item>
        <el-form-item label="章节类型" prop="chapterType">
          <el-radio-group v-model="chapterForm.chapterType">
            <el-radio
              v-for="(item, index) in chapterTypeOptions"
              :key="index"
              :label="item.value"
            >{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="章节难度" prop="difficultyLevel">
          <el-select v-model="chapterForm.difficultyLevel" placeholder="请选择章节难度" clearable style="width: 100%;">
            <el-option
              v-for="(item, index) in difficultyLevelOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="章节排序" prop="chapterOrder">
          <el-input v-model.number="chapterForm.chapterOrder" type="number" placeholder="请输入章节排序" />
        </el-form-item>
        <el-form-item label="章节说明" prop="chapterDesc">
          <el-input
            v-model="chapterForm.chapterDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入章节说明"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitChapterForm">确 定</el-button>
        <el-button @click="cancelChapterForm">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 添加或修改试卷对话框 -->
    <el-dialog :title="examPaperTitle" :visible.sync="examPaperDialogOpen" width="700px" append-to-body>
      <el-form ref="examPaperForm" :model="examPaperForm" :rules="examPaperRules" label-width="120px">
        <el-form-item label="试卷名称" prop="paperName">
          <el-input v-model="examPaperForm.paperName" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="试卷难度" prop="difficultyLevel">
          <el-select v-model="examPaperForm.difficultyLevel" placeholder="请选择试卷难度" clearable style="width: 100%;">
            <el-option
              v-for="(item, index) in paperDifficultyOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试时长(分钟)" prop="durationMinutes">
          <el-input v-model.number="examPaperForm.durationMinutes" type="number" placeholder="请输入考试时长" />
        </el-form-item>
        <el-form-item label="生成方式" prop="generateType">
          <el-radio-group v-model="examPaperForm.generateType">
            <el-radio
              v-for="(item, index) in generateTypeOptions"
              :key="index"
              :label="item.value"
            >{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="试卷说明" prop="paperDesc">
          <el-input
            v-model="examPaperForm.paperDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入试卷说明"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitExamPaperForm">确 定</el-button>
        <el-button @click="cancelExamPaperForm">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 组卷对话框 -->
    <el-dialog title="试卷组卷" :visible.sync="paperComposeOpen" width="60%" append-to-body>
      <div v-if="currentComposePaper" class="paper-compose-container">
        <div class="compose-header">
          <div class="paper-info">
            <h3>{{ currentComposePaper.paperName }}</h3>
            <div class="paper-meta">
              <el-tag size="small" style="margin-right: 10px;">
                难度：{{ getEnumLabel(currentComposePaper.difficultyLevel, difficultyLevelEnumMap) }}
              </el-tag>
              <el-tag size="small" style="margin-right: 10px;">
                题目数量：{{ paperQuestionList.length }}
              </el-tag>
              <el-tag size="small" type="success" style="margin-right: 10px;">
                总分：{{ totalPaperScore }}
              </el-tag>
  </div>
          </div>
          <div class="compose-actions">
            <el-button
              type="primary"
              size="small"
              icon="el-icon-plus"
              @click="handleAddQuestionToPaper"
            >添加题目</el-button>
            <el-button
              v-if="currentComposePaper && currentComposePaper.generateType === 1"
              type="success"
              size="small"
              icon="el-icon-magic-stick"
              @click="handleAiGenerateQuestions"
              :loading="aiGenerating"
            >AI生成</el-button>
          </div>
        </div>

        <!-- 题目列表（按类型从上到下：单选、多选、填空、判断、主观） -->
        <div class="paper-question-sections">
          <div
            v-for="typeValue in paperComposeTypeOrder"
            :key="typeValue"
            class="paper-question-section"
          >
            <div class="section-title">{{ questionTypeEnumMap[typeValue] }}</div>
            <el-table
              v-loading="paperQuestionLoading"
              :data="getPaperQuestionsByType(typeValue)"
              border
              style="width: 100%; margin-top: 10px;"
              size="small"
            >
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="题目标题" prop="questionTitle" align="center" :show-overflow-tooltip="true" min-width="200" />
              <el-table-column label="难度等级" prop="difficultyLevel" align="center" width="100">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.difficultyLevel === 1 ? 'success' : scope.row.difficultyLevel === 2 ? 'warning' : 'danger'">
                    {{ getEnumLabel(scope.row.difficultyLevel, questionDifficultyEnumMap) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="题目分值" prop="questionScore" align="center" width="200">
                <template slot-scope="scope">
                  <el-input-number
                    v-model="scope.row.questionScore"
                    :min="0"
                    :precision="1"
                    size="small"
                    style="width: 160px;"
                    @change="handleQuestionScoreChange"
                  />
                </template>
              </el-table-column>
              <el-table-column label="排序" prop="sortOrder" align="center" width="200">
                <template slot-scope="scope">
                  <el-input-number
                    v-model="scope.row.sortOrder"
                    :min="1"
                    size="small"
                    style="width: 160px;"
                    @change="handleQuestionSortChange"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="100" fixed="right">
                <template slot-scope="scope">
                  <el-button
                    type="text"
                    size="small"
                    style="color: #f56c6c;"
                    @click="handleRemoveQuestionFromPaper(scope.row)"
                  >移除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="savePaperQuestions">保存</el-button>
        <el-button @click="paperComposeOpen = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 添加题目到试卷对话框 -->
    <el-dialog title="添加题目" :visible.sync="addQuestionToPaperOpen" width="70%" append-to-body>
      <div class="add-question-container">
        <!-- 搜索表单 -->
        <el-form :inline="true" :model="chapterQuestionQueryParams" size="small" style="margin-bottom: 15px;">
          <el-form-item label="题目标题">
            <el-input
              v-model="chapterQuestionQueryParams.questionTitle"
              placeholder="请输入题目标题"
              clearable
              @keyup.enter.native="handleChapterQuestionQuery"
            />
          </el-form-item>
          <el-form-item label="难度等级">
            <el-select
              v-model="chapterQuestionQueryParams.difficultyLevel"
              placeholder="请选择难度等级"
              clearable
            >
              <el-option
                v-for="(item, index) in questionDifficultyOptions"
                :key="index"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="handleChapterQuestionQuery">查询</el-button>
            <el-button @click="resetChapterQuestionQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 按题目类型分tab展示 -->
        <el-tabs v-model="chapterQuestionActiveTab" @tab-click="handleChapterQuestionTabClick">
          <el-tab-pane
            v-for="(typeItem, typeIndex) in questionTypeOptions"
            :key="typeIndex"
            :label="typeItem.label"
            :name="String(typeItem.value)"
          >
            <!-- 题目列表 -->
            <el-table
              v-loading="chapterQuestionLoading"
              :data="getFilteredChapterQuestionsByType(typeItem.value)"
              border
              style="width: 100%; margin-top: 10px;"
              @selection-change="handleChapterQuestionSelectionChange"
            >
              <el-table-column type="selection" width="55" :selectable="checkQuestionSelectable" />
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="题目标题" prop="questionTitle" align="center" :show-overflow-tooltip="true" min-width="200" />
              <el-table-column label="难度等级" prop="difficultyLevel" align="center" width="100">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.difficultyLevel === 1 ? 'success' : scope.row.difficultyLevel === 2 ? 'warning' : 'danger'">
                    {{ getEnumLabel(scope.row.difficultyLevel, questionDifficultyEnumMap) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="题目分值" prop="score" align="center" width="100" />
            </el-table>

            <!-- 分页 -->
            <pagination
              v-show="chapterQuestionTotal > 0"
              :total="chapterQuestionTotal"
              :current-page.sync="chapterQuestionQueryParams.current"
              :page-size.sync="chapterQuestionQueryParams.size"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleChapterQuestionSizeChange"
              @current-change="handleChapterQuestionCurrentChange"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="confirmAddQuestionsToPaper">添加选中题目</el-button>
        <el-button @click="addQuestionToPaperOpen = false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  pageEduCourseChapter,
  getEduCourseChapterById,
  saveEduCourseChapter,
  updateEduCourseChapter,
  deleteEduCourseChapter,
  listEduCourse,
  listEduCourseChapter
} from '@/api/edu_course_chapter'
import { pageEduCourse, getEduCourseById, updateEduCourse } from '@/api/edu_course'
import {
  pageEduChapterContent,
  getEduChapterContentById,
  saveEduChapterContent,
  updateEduChapterContent,
  deleteEduChapterContent
} from '@/api/edu_chapter_content'
import {
  pageEduQuestionBank,
  getEduQuestionBankById,
  saveEduQuestionBank,
  updateEduQuestionBank,
  deleteEduQuestionBank,
  listEduQuestionBank
} from '@/api/edu_question_bank'
import {
  listEduChapterExamPaper,
  getEduChapterExamPaperById,
  saveEduChapterExamPaper,
  updateEduChapterExamPaper,
  deleteEduChapterExamPaper
} from '@/api/edu_chapter_exam_paper'
import {
  listEduQuestionOption,
  saveEduQuestionOption,
  updateEduQuestionOption,
  deleteEduQuestionOption,
  deleteBatchEduQuestionOption
} from '@/api/edu_question_option'
import {
  listEduExamPaperQuestion,
  saveEduExamPaperQuestion,
  deleteEduExamPaperQuestion,
  deleteBatchEduExamPaperQuestion,
  aiGeneratePaperQuestions
} from '@/api/edu_exam_paper_question'
import ImagePreview from '@/components/ImagePreview'
import VideoUpload from '@/components/VideoUpload'
import PdfUpload from '@/components/PdfUpload'
import ImgUpload from '@/components/ImgUpload'
import VideoPlayer from '@/components/VideoPlayer'
import PdfViewer from '@/components/PdfViewer'
import Pagination from '@/components/Pagination'
import { mapGetters } from 'vuex'

export default {
  name: 'CourseDetail',
  components: {
    ImagePreview,
    VideoUpload,
    PdfUpload,
    ImgUpload,
    VideoPlayer,
    PdfViewer,
    Pagination
  },
  data() {
    return {
      // 课程列表相关
      courseList: [],
      filteredCourseList: [],
      courseSearchKeyword: '',
      courseLoading: false,
      courseTotal: 0,
      courseQueryParams: {
        current: 1,
        size: 10
        // 不限制审核状态，显示所有课程
      },
      selectedCourseId: null,
      selectedCourse: null,

      // 章节列表相关
      chapterList: [],
      chapterLoading: false,
      chapterTotal: 0,
      chapterQueryParams: {
        current: 1,
        size: 10,
        courseId: null,
        chapterName: null,
        chapterType: null,
        difficultyLevel: null
      },

      // 章节表单相关
      chapterDialogOpen: false,
      chapterTitle: '',
      chapterForm: {},
      chapterRules: {
        chapterName: [
          { required: true, message: '章节名称不能为空', trigger: 'blur' }
        ],
        chapterType: [
          { required: true, message: '章节类型不能为空', trigger: 'change' }
        ],
        difficultyLevel: [
          { required: true, message: '章节难度不能为空', trigger: 'change' }
        ],
        chapterOrder: [
          { required: true, message: '章节排序不能为空', trigger: 'blur' }
        ]
      },

      // 内容维护相关
      contentManageOpen: false,
      currentChapter: null,
      contentList: [],
      contentLoading: false,
      contentDialogOpen: false,
      contentTitle: '',
      contentForm: {},
      // 视频/文档查看相关
      videoViewOpen: false,
      currentVideoUrl: '',
      pdfViewOpen: false,
      currentPdfUrl: '',
      pdfViewKey: 0, // 用于强制重新渲染PDF组件
      // 视频/文档查看相关
      videoViewOpen: false,
      currentVideoUrl: '',
      contentRules: {
        contentTitle: [
          { required: true, message: '内容标题不能为空', trigger: 'blur' }
        ],
        contentType: [
          { required: true, message: '内容类型不能为空', trigger: 'change' }
        ],
        videoUrl: [
          {
            validator: (rule, value, callback) => {
              if (this.contentForm.contentType === 1 && (!value || value.trim() === '')) {
                callback(new Error('请上传视频文件'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        docUrl: [
          {
            validator: (rule, value, callback) => {
              if (this.contentForm.contentType === 2 && (!value || value.trim() === '')) {
                callback(new Error('请上传PDF文档'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        sortOrder: [
          { required: true, message: '排序不能为空', trigger: 'blur' }
        ]
      },
      contentTypeOptions: [{ value: 1, label: '学习视频' }, { value: 2, label: '学习文档' }],
      contentTypeEnumMap: { 1: '学习视频', 2: '学习文档' },

      // 测试题管理相关
      questionManageOpen: false,
      currentQuestionChapter: null,
      questionList: [],
      questionLoading: false,
      questionActiveTab: 'single', // 当前选中的题目类型tab
      questionQueryParams: {
        questionTitle: null,
        difficultyLevel: null,
        questionType: 1 // 默认单选题，chapterId在查询时动态设置
      },
      questionDialogOpen: false,
      questionTitle: '',
      questionForm: {},
      questionRules: {
        questionTitle: [
          { required: true, message: '题目标题不能为空', trigger: 'blur' }
        ],
        questionType: [
          { required: true, message: '题目类型不能为空', trigger: 'change' }
        ],
        difficultyLevel: [
          { required: true, message: '难度等级不能为空', trigger: 'change' }
        ],
        answerContent: [
          { required: true, message: '答案内容不能为空', trigger: 'blur' }
        ],
        score: [
          { required: true, message: '题目分值不能为空', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '题目状态不能为空', trigger: 'change' }
        ]
      },
      questionTypeOptions: [
        { value: 1, label: '单选题' },
        { value: 2, label: '多选题' },
        { value: 3, label: '判断题' },
        { value: 4, label: '填空题' },
        { value: 5, label: '主观题' }
      ],
      questionDifficultyOptions: [
        { value: 1, label: '低' },
        { value: 2, label: '中' },
        { value: 3, label: '高' }
      ],
      questionStatusOptions: [
        { value: 0, label: '停用' },
        { value: 1, label: '启用' }
      ],
      questionTypeEnumMap: { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '主观题' },
      questionDifficultyEnumMap: { 1: '低', 2: '中', 3: '高' },
      questionStatusEnumMap: { 0: '停用', 1: '启用' },

      // 试卷管理相关
      expandedChapterRows: [], // 展开的行
      examPaperDialogOpen: false,
      examPaperTitle: '',
      examPaperForm: {},
      examPaperRules: {
        paperName: [
          { required: true, message: '试卷名称不能为空', trigger: 'blur' }
        ],
        difficultyLevel: [
          { required: true, message: '试卷难度不能为空', trigger: 'change' }
        ],
        durationMinutes: [
          { required: true, message: '考试时长不能为空', trigger: 'blur' }
        ],
        generateType: [
          { required: true, message: '生成方式不能为空', trigger: 'change' }
        ]
      },
      paperDifficultyOptions: [{ value: 1, label: '低' }, { value: 2, label: '中' }, { value: 3, label: '高' }],
      generateTypeOptions: [{ value: 1, label: 'AI组卷' }, { value: 2, label: '手动组卷' }],
      paperStatusOptions: [{ value: 0, label: '停用' }, { value: 1, label: '启用' }],
      paperStatusEnumMap: { 0: '停用', 1: '启用' },
      generateTypeEnumMap: { 1: 'AI组卷', 2: '手动组卷' },
      difficultyLevelEnumMap: { 1: '简单', 2: '中等', 3: '困难' },
      currentExamPaperChapter: null, // 当前操作的试卷所属章节

      // 组卷相关
      paperComposeOpen: false,
      currentComposePaper: null, // 当前组卷的试卷
      currentComposeChapter: null, // 当前组卷的章节
      paperQuestionList: [], // 试卷题目列表
      paperQuestionLoading: false,
      paperComposeTypeOrder: [1, 2, 4, 3, 5], // 组卷展示顺序：单选、多选、填空、判断、主观
      totalPaperScore: 0, // 试卷总分
      aiGenerating: false, // AI生成中

      // 添加题目到试卷相关
      addQuestionToPaperOpen: false,
      chapterQuestionList: [], // 章节题目列表（所有类型）
      chapterQuestionLoading: false,
      chapterQuestionTotal: 0,
      chapterQuestionActiveTab: '1', // 当前选中的题目类型tab
      chapterQuestionQueryParams: {
        current: 1,
        size: 10,
        questionTitle: null,
        questionType: null,
        difficultyLevel: null
      },
      selectedChapterQuestions: [], // 选中的题目

      // 枚举映射
      chapterTypeOptions: [{ value: 1, label: '基础章节' }, { value: 2, label: '难度章节' }],
      difficultyLevelOptions: [{ value: 1, label: '简单' }, { value: 2, label: '中等' }, { value: 3, label: '困难' }],
      chapterTypeEnumMap: { 1: '基础章节', 2: '难度章节' },
      difficultyLevelEnumMap: { 1: '简单', 2: '中等', 3: '困难' },
      auditStatusEnumMap: { 0: '待审核', 1: '通过', 2: '驳回' }
    }
  },
  computed: {
    ...mapGetters(['role', 'id']),
    userRole() {
      return this.role || ''
    },
    userId() {
      return this.id || null
    },
    /** 根据类型过滤的章节题目（返回一个函数） */
    getFilteredChapterQuestionsByType() {
      return (type) => {
        const typeNum = typeof type === 'string' ? parseInt(type) : type
        return this.chapterQuestionList.filter(q => {
          const qType = typeof q.questionType === 'string' ? parseInt(q.questionType) : q.questionType
          return qType === typeNum
        })
      }
    }
  },
  created() {
    this.getCourseList()
  },
  methods: {
    /** 获取课程列表（老师只能查看自己的课程） */
    getCourseList() {
      this.courseLoading = true
      const { current, size, ...rest } = this.courseQueryParams
      // 构建查询参数，只查询草稿状态的课程（auditStatus = 3）
      const queryParams = {
        auditStatus: 3 // 只显示草稿状态的课程
      }
      // 只传递必要的查询参数
      if (rest.courseName) queryParams.courseName = rest.courseName
      if (rest.difficultyLevel != null) queryParams.difficultyLevel = rest.difficultyLevel
      if (rest.status != null) queryParams.status = rest.status
      if (rest.realName) queryParams.realName = rest.realName
      if (rest.teacherId) queryParams.teacherId = rest.teacherId
      // 老师角色仅查看自己的课程
      if (this.userRole === 'teacher' && this.userId) {
        queryParams.teacherId = this.userId
      }
      pageEduCourse(current, size, queryParams).then(response => {
        const pageData = response.data
        this.courseList = pageData.records || []
        this.filteredCourseList = this.courseList
        // 使用后端返回的总数
        this.courseTotal = pageData.total || 0
        this.courseLoading = false
        // 如果有课程且未选择，默认选择第一个
        if (this.courseList.length > 0 && !this.selectedCourseId) {
          this.selectCourse(this.courseList[0])
        }
      }).catch((error) => {
        console.error('获取课程列表失败:', error)
        this.courseLoading = false
      })
    },
    /** 课程搜索 */
    handleCourseSearch() {
      if (!this.courseSearchKeyword || this.courseSearchKeyword.trim() === '') {
        this.filteredCourseList = this.courseList
      } else {
        const keyword = this.courseSearchKeyword.toLowerCase()
        this.filteredCourseList = this.courseList.filter(course => {
          return course.courseName && course.courseName.toLowerCase().includes(keyword)
        })
      }
    },
    /** 选择课程 */
    selectCourse(course) {
      this.selectedCourseId = course.id
      this.selectedCourse = course
      this.chapterQueryParams.courseId = course.id
      this.chapterQueryParams.current = 1
      this.getChapterList()
    },
    /** 课程分页 */
    handleCourseSizeChange(val) {
      this.courseQueryParams.size = val
      this.courseQueryParams.current = 1
      this.getCourseList()
    },
    handleCourseCurrentChange(val) {
      this.courseQueryParams.current = val
      this.getCourseList()
    },

    /** 获取章节列表 */
    getChapterList() {
      if (!this.selectedCourseId) {
        this.chapterList = []
        return
      }
      this.chapterLoading = true
      const { current, size, ...rest } = this.chapterQueryParams
      pageEduCourseChapter(current, size, rest).then(response => {
        const pageData = response.data
        let chapters = pageData.records || []
        // 对章节进行排序：先按章节类型，再按难度
        chapters = chapters.sort((a, b) => {
          // 先按章节类型排序：1（基础章节）在前，2（难度章节）在后
          if (a.chapterType !== b.chapterType) {
            return a.chapterType - b.chapterType
          }
          // 同一类型内，按难度排序：1（简单）< 2（中等）< 3（困难）
          return a.difficultyLevel - b.difficultyLevel
        })
        // 为每个章节初始化试卷列表相关属性
        chapters.forEach(chapter => {
          this.$set(chapter, 'examPaperList', [])
          this.$set(chapter, 'examPaperLoading', false)
          // 加载试卷数量
          this.loadChapterExamPaperCount(chapter)
        })
        this.chapterList = chapters
        this.chapterTotal = Number(pageData.total) || 0
        this.chapterLoading = false
      }).catch(() => {
        this.chapterLoading = false
      })
    },
    /** 章节查询 */
    handleChapterQuery() {
      this.chapterQueryParams.current = 1
      this.getChapterList()
    },
    /** 重置章节查询 */
    resetChapterQuery() {
      this.chapterQueryParams.chapterName = null
      this.chapterQueryParams.chapterType = null
      this.chapterQueryParams.difficultyLevel = null
      this.chapterQueryParams.current = 1
      this.handleChapterQuery()
    },
    /** 章节分页 */
    handleChapterSizeChange(val) {
      this.chapterQueryParams.size = val
      this.chapterQueryParams.current = 1
      this.getChapterList()
    },
    handleChapterCurrentChange(val) {
      this.chapterQueryParams.current = val
      this.getChapterList()
    },

    /** 新建章节 */
    handleAddChapter() {
      this.resetChapterForm()
      this.chapterForm.courseId = this.selectedCourseId
      this.chapterDialogOpen = true
      this.chapterTitle = '添加章节'
    },
    /** 修改章节 */
    handleUpdateChapter(row) {
      this.resetChapterForm()
      const id = row.id
      getEduCourseChapterById(id).then(response => {
        this.chapterForm = response.data
        this.chapterDialogOpen = true
        this.chapterTitle = '修改章节'
      })
    },
    /** 删除章节 */
    handleDeleteChapter(row) {
      const ids = row.id
      this.$confirm('是否确认删除该章节？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteEduCourseChapter(ids)
      }).then(() => {
        this.getChapterList()
        this.$message.success('删除成功')
        // 更新课程的章节数量
        this.updateCourseChapterCount()
      }).catch(() => {})
    },
    /** 提交章节表单 */
    submitChapterForm() {
      this.$refs['chapterForm'].validate(valid => {
        if (valid) {
          if (this.chapterForm.id != null) {
            updateEduCourseChapter(this.chapterForm).then(response => {
              this.$message.success('修改成功')
              this.chapterDialogOpen = false
              this.getChapterList()
              // 刷新课程列表以更新章节数量
              this.getCourseList()
            })
          } else {
            saveEduCourseChapter(this.chapterForm).then(response => {
              this.$message.success('新增成功')
              this.chapterDialogOpen = false
              this.getChapterList()
              // 更新课程的章节数量
              this.updateCourseChapterCount()
              // 刷新课程列表以更新章节数量
              this.getCourseList()
            })
          }
        }
      })
    },
    /** 更新课程的章节数量 */
    updateCourseChapterCount() {
      if (!this.selectedCourseId) {
        return
      }
      // 获取该课程的所有章节
      listEduCourseChapter({ courseId: this.selectedCourseId }).then(response => {
        const chapters = response.data || []
        const chapterCount = chapters.length
        // 更新课程的章节数量
        getEduCourseById(this.selectedCourseId).then(courseResponse => {
          const course = courseResponse.data
          if (course) {
            updateEduCourse({
              id: course.id,
              chapterCount: chapterCount
            }).then(() => {
              // 更新当前选中的课程信息
              if (this.selectedCourse) {
                this.selectedCourse.chapterCount = chapterCount
              }
              // 更新左侧课程列表中对应课程的章节数量
              const courseIndex = this.courseList.findIndex(c => c.id === this.selectedCourseId)
              if (courseIndex !== -1) {
                this.$set(this.courseList, courseIndex, {
                  ...this.courseList[courseIndex],
                  chapterCount: chapterCount
                })
                // 同时更新过滤后的列表
                const filteredIndex = this.filteredCourseList.findIndex(c => c.id === this.selectedCourseId)
                if (filteredIndex !== -1) {
                  this.$set(this.filteredCourseList, filteredIndex, {
                    ...this.filteredCourseList[filteredIndex],
                    chapterCount: chapterCount
                  })
                }
              }
            }).catch(() => {
              console.error('更新课程章节数量失败')
            })
          }
        }).catch(() => {
          console.error('获取课程信息失败')
        })
      }).catch(() => {
        console.error('获取章节列表失败')
      })
    },
    /** 取消章节表单 */
    cancelChapterForm() {
      this.chapterDialogOpen = false
      this.resetChapterForm()
    },
    /** 重置章节表单 */
    resetChapterForm() {
      this.chapterForm = {}
      if (this.$refs.chapterForm) {
        this.$refs.chapterForm.resetFields()
      }
    },
    /** 枚举列展示 */
    getEnumLabel(val, map) {
      if (map == null || map[val] === undefined) return val
      return map[val]
    },
    /** 内容维护按钮操作 */
    handleContentManage(row) {
      this.currentChapter = row
      this.contentManageOpen = true
      this.getContentList()
    },
    /** 获取章节内容列表 */
    getContentList() {
      if (!this.currentChapter || !this.currentChapter.id) {
        this.contentList = []
        return
      }
      this.contentLoading = true
      pageEduChapterContent(1, 1000, { chapterId: this.currentChapter.id }).then(response => {
        const pageData = response.data
        this.contentList = pageData.records || []
        this.contentLoading = false
      }).catch(() => {
        this.contentLoading = false
      })
    },
    /** 新建内容 */
    handleAddContent() {
      this.resetContentForm()
      this.contentForm.chapterId = this.currentChapter.id
      this.contentDialogOpen = true
      this.contentTitle = '添加内容'
    },
    /** 修改内容 */
    handleUpdateContent(row) {
      this.resetContentForm()
      const id = row.id
      getEduChapterContentById(id).then(response => {
        this.contentForm = response.data
        this.contentDialogOpen = true
        this.contentTitle = '修改内容'
      })
    },
    /** 删除内容 */
    handleDeleteContent(row) {
      const ids = row.id
      this.$confirm('是否确认删除该内容？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteEduChapterContent(ids)
      }).then(() => {
        this.getContentList()
        this.$message.success('删除成功')
        // 更新章节的内容数量
        this.updateChapterContentCount()
      }).catch(() => {})
    },
    /** 提交内容表单 */
    submitContentForm() {
      this.$refs['contentForm'].validate(valid => {
        if (valid) {
          if (this.contentForm.id != null) {
            updateEduChapterContent(this.contentForm).then(response => {
              this.$message.success('修改成功')
              this.contentDialogOpen = false
              this.getContentList()
              this.updateChapterContentCount()
            })
          } else {
            saveEduChapterContent(this.contentForm).then(response => {
              this.$message.success('新增成功')
              this.contentDialogOpen = false
              this.getContentList()
              this.updateChapterContentCount()
            })
          }
        }
      })
    },
    /** 取消内容表单 */
    cancelContentForm() {
      this.contentDialogOpen = false
      this.resetContentForm()
    },
    /** 内容类型改变处理 */
    handleContentTypeChange(value) {
      // 切换内容类型时，清空对应的URL字段
      if (value === 1) {
        // 切换到学习视频，清空文档地址
        this.contentForm.docUrl = ''
      } else if (value === 2) {
        // 切换到学习文档，清空视频地址
        this.contentForm.videoUrl = ''
      }
    },
    /** 重置内容表单 */
    resetContentForm() {
      this.contentForm = {}
      if (this.$refs.contentForm) {
        this.$refs.contentForm.resetFields()
      }
    },
    /** 封面/图片完整 URL（相对路径时拼接 baseAPI） */
    getFullImageUrl(url) {
      if (!url || typeof url !== 'string') return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      const base = process.env.VUE_APP_BASE_API || ''
      return base.replace(/\/$/, '') + (url.startsWith('/') ? url : '/' + url)
    },
    /** 查看视频 */
    handleViewVideo(videoUrl) {
      this.currentVideoUrl = videoUrl
      this.videoViewOpen = true
    },
    /** 查看PDF文档 */
    handleViewPdf(pdfUrl) {
      // 增加key值，强制重新渲染组件
      this.pdfViewKey++
      // 先重置URL，确保组件会重新创建
      this.currentPdfUrl = ''
      // 使用nextTick确保DOM更新后再设置新的URL
      this.$nextTick(() => {
        this.currentPdfUrl = pdfUrl
        if (!this.pdfViewOpen) {
          this.pdfViewOpen = true
        }
      })
    },
    /** PDF对话框关闭时的处理 */
    handlePdfDialogClose() {
      // 重置PDF URL和key，确保下次打开时组件会重新创建
      this.currentPdfUrl = ''
      this.pdfViewKey = 0
    },
    /** 更新章节的内容数量 */
    updateChapterContentCount() {
      if (!this.currentChapter || !this.currentChapter.id) {
        return
      }
      // 获取该章节的所有内容
      pageEduChapterContent(1, 1000, { chapterId: this.currentChapter.id }).then(response => {
        const pageData = response.data
        const contents = pageData.records || []
        const contentCount = contents.length
        // 更新章节的内容数量
        updateEduCourseChapter({
          id: this.currentChapter.id,
          contentCount: contentCount
        }).then(() => {
          // 更新当前章节信息
          this.currentChapter.contentCount = contentCount
          // 更新章节列表中的章节信息
          const chapterIndex = this.chapterList.findIndex(c => c.id === this.currentChapter.id)
          if (chapterIndex !== -1) {
            this.$set(this.chapterList, chapterIndex, {
              ...this.chapterList[chapterIndex],
              contentCount: contentCount
            })
          }
          // 刷新章节列表以确保数据同步
          this.getChapterList()
        }).catch((error) => {
          console.error('更新章节内容数量失败:', error)
        })
      }).catch((error) => {
        console.error('获取内容列表失败:', error)
      })
    },
    /** 章节测试题按钮操作 */
    handleQuestionManage(row) {
      this.currentQuestionChapter = row
      this.questionManageOpen = true
      this.questionActiveTab = 'single'
      this.setQuestionTypeByTab('single')
      // 重置查询参数
      this.questionQueryParams.questionTitle = null
      this.questionQueryParams.difficultyLevel = null
      this.getQuestionList()
    },
    /** 根据tab设置题目类型 */
    setQuestionTypeByTab(tabName) {
      switch (tabName) {
        case 'single':
          this.questionQueryParams.questionType = 1 // 单选题
          break
        case 'multiple':
          this.questionQueryParams.questionType = 2 // 多选题
          break
        case 'judge':
          this.questionQueryParams.questionType = 3 // 判断题
          break
        case 'fill':
          this.questionQueryParams.questionType = 4 // 填空题
          break
        case 'subjective':
          this.questionQueryParams.questionType = 5 // 主观题
          break
        default:
          this.questionQueryParams.questionType = 1
      }
    },
    /** Tab切换处理 */
    handleQuestionTabClick(tab) {
      this.setQuestionTypeByTab(tab.name)
      this.getQuestionList()
    },
    /** 获取题目列表 */
    getQuestionList() {
      if (!this.currentQuestionChapter || !this.currentQuestionChapter.id) {
        this.questionList = []
        return
      }
      this.questionLoading = true
      // 确保只查询当前章节的题目
      const queryParams = {
        ...this.questionQueryParams,
        chapterId: this.currentQuestionChapter.id
      }
      pageEduQuestionBank(1, 1000, queryParams).then(response => {
        const pageData = response.data
        this.questionList = pageData.records || []
        this.questionLoading = false
      }).catch(() => {
        this.questionLoading = false
      })
    },
    /** 题目查询 */
    handleQuestionQuery() {
      this.getQuestionList()
    },
    /** 重置题目查询 */
    resetQuestionQuery() {
      this.questionQueryParams.questionTitle = null
      this.questionQueryParams.difficultyLevel = null
      // 保持当前tab对应的题目类型
      this.setQuestionTypeByTab(this.questionActiveTab)
      this.handleQuestionQuery()
    },
    /** 新建题目 */
    handleAddQuestion() {
      this.resetQuestionForm()
      this.questionForm.chapterId = this.currentQuestionChapter.id
      this.questionForm.courseId = this.selectedCourseId
      // 根据当前tab设置题目类型
      this.questionForm.questionType = this.questionQueryParams.questionType
      this.questionDialogOpen = true
      this.questionTitle = '添加题目'
    },
    /** 修改题目 */
    handleUpdateQuestion(row) {
      this.resetQuestionForm()
      const id = row.id
      getEduQuestionBankById(id).then(response => {
        // 先设置题目基本信息
        this.questionForm = { ...response.data, questionOptions: [] }
        // 如果是需要选项的题目类型，加载题目的选项
        if (this.questionForm.questionType === 1 || this.questionForm.questionType === 2 || this.questionForm.questionType === 4) {
          // 使用$nextTick确保questionForm已更新后再加载选项
          this.$nextTick(() => {
            this.loadQuestionOptions(id)
          })
        }
        this.questionDialogOpen = true
        this.questionTitle = '修改题目'
      }).catch(() => {
        this.$message.error('获取题目信息失败')
      })
    },
    /** 加载题目选项 */
    loadQuestionOptions(questionId) {
      listEduQuestionOption({ questionId: questionId }).then(response => {
        const options = response.data || []
        // 按sortOrder排序（后端已经排序，这里再确保一下）
        options.sort((a, b) => {
          const sortA = a.sortOrder || a.sort_order || 0
          const sortB = b.sortOrder || b.sort_order || 0
          return sortA - sortB
        })
        // 确保questionForm.questionOptions已初始化
        if (!this.questionForm.questionOptions) {
          this.$set(this.questionForm, 'questionOptions', [])
        }
        // 映射字段，兼容驼峰和下划线格式
        this.$set(this.questionForm, 'questionOptions', options.map((opt, idx) => {
          // 如果optionLabel为空，根据题目类型自动生成
          let optionLabel = opt.optionLabel || opt.option_label
          if (!optionLabel) {
            if (this.questionForm.questionType === 4) {
              optionLabel = `填空${idx + 1}`
            } else {
              optionLabel = String.fromCharCode(65 + idx) // A, B, C, D...
            }
          }
          return {
            id: opt.id,
            optionLabel: optionLabel,
            optionContent: opt.optionContent || opt.option_content || '',
            isCorrect: opt.isCorrect !== undefined && opt.isCorrect !== null ? opt.isCorrect : (opt.is_correct !== undefined && opt.is_correct !== null ? opt.is_correct : 0),
            sortOrder: opt.sortOrder || opt.sort_order || 0
          }
        }))
      }).catch((error) => {
        console.error('加载选项失败:', error)
        this.$message.error('加载题目选项失败')
        if (!this.questionForm.questionOptions) {
          this.$set(this.questionForm, 'questionOptions', [])
        } else {
          this.$set(this.questionForm, 'questionOptions', [])
        }
      })
    },
    /** 删除题目 */
    handleDeleteQuestion(row) {
      const ids = row.id
      this.$confirm('是否确认删除该题目？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteEduQuestionBank(ids)
      }).then(() => {
        this.getQuestionList()
        this.$message.success('删除成功')
        // 更新章节的题目数量
        this.updateChapterQuestionCount()
      }).catch(() => {})
    },
    /** 提交题目表单 */
    submitQuestionForm() {
      this.$refs['questionForm'].validate(valid => {
        if (valid) {
          // 验证选项（针对单选题、多选题、填空题）
          if (this.questionForm.questionType === 1 || this.questionForm.questionType === 2 || this.questionForm.questionType === 4) {
            if (!this.questionForm.questionOptions || this.questionForm.questionOptions.length === 0) {
              this.$message.warning('请至少添加一个选项')
              return
            }
            // 验证选项内容
            for (let i = 0; i < this.questionForm.questionOptions.length; i++) {
              const option = this.questionForm.questionOptions[i]
              if (!option.optionContent || option.optionContent.trim() === '') {
                const label = this.questionForm.questionType === 4 ? `填空${i + 1}` : this.getOptionLabel(i)
                this.$message.warning(`${label}的内容不能为空`)
                return
              }
            }
            // 验证正确答案（单选题和多选题）
            if (this.questionForm.questionType === 1 || this.questionForm.questionType === 2) {
              const correctOptions = this.questionForm.questionOptions.filter(opt => opt.isCorrect === 1)
              if (correctOptions.length === 0) {
                this.$message.warning('请至少选择一个正确答案')
                return
              }
              if (this.questionForm.questionType === 1 && correctOptions.length > 1) {
                this.$message.warning('单选题只能有一个正确答案')
                return
              }
              // 设置答案内容为正确答案的选项标识
              this.questionForm.answerContent = correctOptions.map(opt => opt.optionLabel).join(',')
            } else if (this.questionForm.questionType === 4) {
              // 填空题：答案内容为所有选项内容
              this.questionForm.answerContent = this.questionForm.questionOptions.map(opt => opt.optionContent).join('|')
            }
          }
          
          // 保存题目
          const saveQuestionPromise = this.questionForm.id != null
            ? updateEduQuestionBank(this.questionForm)
            : saveEduQuestionBank(this.questionForm)
          
          saveQuestionPromise.then(response => {
            const questionId = response.data.id || this.questionForm.id
            // 保存选项（针对单选题、多选题、填空题）
            if (this.questionForm.questionType === 1 || this.questionForm.questionType === 2 || this.questionForm.questionType === 4) {
              this.saveQuestionOptions(questionId)
            } else {
              this.$message.success(this.questionForm.id != null ? '修改成功' : '新增成功')
              this.questionDialogOpen = false
              this.getQuestionList()
              this.updateChapterQuestionCount()
            }
          }).catch(() => {
            this.$message.error(this.questionForm.id != null ? '修改失败' : '新增失败')
          })
        }
      })
    },
    /** 保存题目选项 */
    saveQuestionOptions(questionId) {
      // 如果没有选项，直接返回成功
      if (!this.questionForm.questionOptions || this.questionForm.questionOptions.length === 0) {
        this.$message.success(this.questionForm.id != null ? '修改成功' : '新增成功')
        this.questionDialogOpen = false
        this.getQuestionList()
        this.updateChapterQuestionCount()
        return
      }
      
      // 如果是修改，先删除所有旧选项，然后全部作为新增保存（简化逻辑）
      if (this.questionForm.id != null) {
        listEduQuestionOption({ questionId: questionId }).then(response => {
          const oldOptions = response.data || []
          if (oldOptions.length > 0) {
            const oldIds = oldOptions.map(opt => opt.id)
            return deleteBatchEduQuestionOption(oldIds)
          }
          return Promise.resolve()
        }).then(() => {
          // 删除成功后，全部作为新增保存
          this.saveNewOptions(questionId)
        }).catch((error) => {
          console.error('删除旧选项失败:', error)
          this.$message.error('删除旧选项失败')
        })
      } else {
        // 新增题目，直接保存选项
        this.saveNewOptions(questionId)
      }
    },
    /** 保存新选项（全部作为新增） */
    saveNewOptions(questionId) {
      const optionPromises = this.questionForm.questionOptions.map((option, index) => {
        const optionData = {
          questionId: questionId,
          optionLabel: option.optionLabel || this.getOptionLabel(index),
          optionContent: option.optionContent,
          isCorrect: option.isCorrect !== undefined && option.isCorrect !== null ? option.isCorrect : 0,
          sortOrder: index + 1
        }
        return saveEduQuestionOption(optionData)
      })
      
      Promise.all(optionPromises).then(() => {
        this.$message.success(this.questionForm.id != null ? '修改成功' : '新增成功')
        this.questionDialogOpen = false
        this.getQuestionList()
        this.updateChapterQuestionCount()
      }).catch((error) => {
        console.error('保存选项失败:', error)
        this.$message.error('保存选项失败')
      })
    },
    /** 添加选项 */
    addQuestionOption() {
      if (!this.questionForm.questionOptions) {
        this.questionForm.questionOptions = []
      }
      const index = this.questionForm.questionOptions.length
      this.questionForm.questionOptions.push({
        optionLabel: this.getOptionLabel(index),
        optionContent: '',
        isCorrect: 0,
        sortOrder: index + 1
      })
    },
    /** 删除选项 */
    removeQuestionOption(index) {
      this.questionForm.questionOptions.splice(index, 1)
      // 重新设置选项标识和排序
      this.questionForm.questionOptions.forEach((option, idx) => {
        option.optionLabel = this.getOptionLabel(idx)
        option.sortOrder = idx + 1
      })
    },
    /** 获取选项标识 */
    getOptionLabel(index) {
      // 填空题使用"填空1"、"填空2"格式
      if (this.questionForm.questionType === 4) {
        return `填空${index + 1}`
      }
      // 单选题和多选题使用A、B、C、D格式
      return String.fromCharCode(65 + index) // A=65, B=66, C=67...
    },
    /** 题目类型变化处理 */
    handleQuestionTypeChange() {
      // 如果切换到不需要选项的类型，清空选项
      if (this.questionForm.questionType !== 1 && this.questionForm.questionType !== 2 && this.questionForm.questionType !== 4) {
        this.questionForm.questionOptions = []
      } else if (!this.questionForm.questionOptions || this.questionForm.questionOptions.length === 0) {
        // 如果是需要选项的类型且没有选项，添加一个默认选项
        this.addQuestionOption()
      }
    },
    /** 取消题目表单 */
    cancelQuestionForm() {
      this.questionDialogOpen = false
      this.resetQuestionForm()
    },
    /** 重置题目表单 */
    resetQuestionForm() {
      this.questionForm = {
        questionOptions: []
      }
      if (this.$refs.questionForm) {
        this.$refs.questionForm.resetFields()
      }
    },
    /** 更新章节的题目数量 */
    updateChapterQuestionCount() {
      if (!this.currentQuestionChapter || !this.currentQuestionChapter.id) {
        return
      }
      // 获取该章节的所有题目
      pageEduQuestionBank(1, 1000, { chapterId: this.currentQuestionChapter.id }).then(response => {
        const pageData = response.data
        const questions = pageData.records || []
        const questionCount = questions.length
        // 更新章节的题目数量
        updateEduCourseChapter({
          id: this.currentQuestionChapter.id,
          questionCount: questionCount
        }).then(() => {
          // 更新当前章节信息
          this.currentQuestionChapter.questionCount = questionCount
          // 更新章节列表中的章节信息
          const chapterIndex = this.chapterList.findIndex(c => c.id === this.currentQuestionChapter.id)
          if (chapterIndex !== -1) {
            this.$set(this.chapterList, chapterIndex, {
              ...this.chapterList[chapterIndex],
              questionCount: questionCount
            })
          }
          // 刷新章节列表以确保数据同步
          this.getChapterList()
        }).catch((error) => {
          console.error('更新章节题目数量失败:', error)
        })
      }).catch((error) => {
        console.error('获取题目列表失败:', error)
      })
    },
    /** 获取章节行key */
    getChapterRowKey(row) {
      return row.id
    },
    /** 章节展开行变化 */
    handleChapterExpandChange(row, expandedRows) {
      if (expandedRows.includes(row)) {
        // 展开时加载试卷列表
        this.loadChapterExamPapers(row)
      }
      this.expandedChapterRows = expandedRows.map(r => r.id)
    },
    /** 加载章节试卷列表 */
    loadChapterExamPapers(chapter) {
      if (!chapter || !chapter.id) {
        return
      }
      this.$set(chapter, 'examPaperLoading', true)
      this.$set(chapter, 'examPaperList', [])
      listEduChapterExamPaper({ chapterId: chapter.id }).then(response => {
        const papers = response.data || []
        this.$set(chapter, 'examPaperList', papers)
        this.$set(chapter, 'examPaperLoading', false)
        // 更新章节的试卷数量
        this.$set(chapter, 'examPaperCount', papers.length)
        // 更新章节列表中的试卷数量
        const chapterIndex = this.chapterList.findIndex(c => c.id === chapter.id)
        if (chapterIndex !== -1) {
          this.$set(this.chapterList[chapterIndex], 'examPaperCount', papers.length)
        }
        // 更新数据库中的试卷数量
        this.updateChapterExamPaperCount(chapter.id, papers.length)
      }).catch((error) => {
        console.error('加载章节试卷失败:', error)
        this.$set(chapter, 'examPaperLoading', false)
        this.$set(chapter, 'examPaperList', [])
      })
    },
    /** 加载章节试卷数量 */
    loadChapterExamPaperCount(chapter) {
      if (!chapter || !chapter.id) {
        return
      }
      listEduChapterExamPaper({ chapterId: chapter.id }).then(response => {
        const papers = response.data || []
        this.$set(chapter, 'examPaperCount', papers.length)
      }).catch(() => {
        this.$set(chapter, 'examPaperCount', 0)
      })
    },
    /** 更新章节的试卷数量 */
    updateChapterExamPaperCount(chapterId, examPaperCount) {
      // 注意：examPaperCount字段在数据库表中不存在，所以不更新数据库
      // 只在前端显示时计算，与contentCount和questionCount的处理方式保持一致
      // 如果需要持久化，需要在数据库表中添加exam_paper_count字段，并在实体类中添加对应属性
    },
    /** 新建试卷 */
    handleAddExamPaper(chapter) {
      this.resetExamPaperForm()
      this.examPaperForm.chapterId = chapter.id
      this.examPaperForm.status = 0 // 默认停用
      this.examPaperForm.generateType = 2 // 默认手动组卷
      // 题目数量和总分不手动输入，由组卷时自动计算
      this.examPaperForm.questionCount = 0
      this.examPaperForm.totalScore = 0
      // 试卷编号不设置，由后端自动生成
      this.examPaperForm.paperCode = ''
      this.examPaperDialogOpen = true
      this.examPaperTitle = '添加试卷'
      this.currentExamPaperChapter = chapter
    },
    /** 修改试卷 */
    handleUpdateExamPaper(row) {
      // 只有停用状态的试卷才能修改
      if (row.status === 1) {
        this.$message.warning('只有停用状态的试卷才能进行修改')
        return
      }
      this.resetExamPaperForm()
      const id = row.id
      getEduChapterExamPaperById(id).then(response => {
        this.examPaperForm = response.data
        this.examPaperDialogOpen = true
        this.examPaperTitle = '修改试卷'
        // 找到试卷所属的章节
        const chapter = this.chapterList.find(c => c.id === row.chapterId)
        this.currentExamPaperChapter = chapter
      })
    },
    /** 启用试卷 */
    handleEnableExamPaper(paper, chapter) {
      // 先检查试卷是否有题目
      listEduExamPaperQuestion({ paperId: paper.id }).then(response => {
        const questions = response.data || []
        if (questions.length === 0) {
          this.$message.warning('试卷中没有题目，无法启用')
          return
        }
        // 计算总分
        const totalScore = questions.reduce((sum, q) => {
          return sum + (Number(q.questionScore) || 0)
        }, 0)
        // 检查总分是否为100分
        if (Math.abs(totalScore - 100) > 0.01) {
          this.$message.warning(`试卷总分必须为100分，当前为${totalScore}分`)
          return
        }
        // 确认启用
        this.$confirm(`确定要启用该试卷吗？试卷共有${questions.length}道题目，总分为${totalScore}分。`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }).then(() => {
          updateEduChapterExamPaper({
            id: paper.id,
            status: 1
          }).then(() => {
            this.$message.success('启用成功')
            // 重新加载当前章节的试卷列表
            this.loadChapterExamPapers(chapter)
          }).catch(() => {
            this.$message.error('启用失败')
          })
        }).catch(() => {})
      }).catch(() => {
        this.$message.error('获取试卷题目失败')
      })
    },
    /** 停用试卷 */
    handleDisableExamPaper(paper, chapter) {
      this.$confirm('确定要停用该试卷吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        updateEduChapterExamPaper({
          id: paper.id,
          status: 0
        }).then(() => {
          this.$message.success('停用成功')
          // 重新加载当前章节的试卷列表
          this.loadChapterExamPapers(chapter)
        }).catch(() => {
          this.$message.error('停用失败')
        })
      }).catch(() => {})
    },
    /** 删除试卷 */
    handleDeleteExamPaper(paper, chapter) {
      // 启用状态的试卷不能删除
      if (paper.status === 1) {
        this.$message.warning('启用状态的试卷不能删除，请先停用')
        return
      }
      const ids = paper.id
      this.$confirm('是否确认删除该试卷？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteEduChapterExamPaper(ids)
      }).then(() => {
        // 重新加载该章节的试卷列表
        this.loadChapterExamPapers(chapter)
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    /** 提交试卷表单 */
    submitExamPaperForm() {
      this.$refs['examPaperForm'].validate(valid => {
        if (valid) {
          if (this.examPaperForm.id != null) {
            updateEduChapterExamPaper(this.examPaperForm).then(response => {
              this.$message.success('修改成功')
              this.examPaperDialogOpen = false
              // 重新加载当前章节的试卷列表
              if (this.currentExamPaperChapter) {
                this.loadChapterExamPapers(this.currentExamPaperChapter)
              }
            })
          } else {
            saveEduChapterExamPaper(this.examPaperForm).then(response => {
              this.$message.success('新增成功')
              this.examPaperDialogOpen = false
              // 重新加载当前章节的试卷列表
              if (this.currentExamPaperChapter) {
                this.loadChapterExamPapers(this.currentExamPaperChapter)
              }
            })
          }
        }
      })
    },
    /** 取消试卷表单 */
    cancelExamPaperForm() {
      this.examPaperDialogOpen = false
      this.resetExamPaperForm()
    },
    /** 重置试卷表单（用完整默认对象保证 generateType 等字段响应式，避免添加试卷时生成方式选不了） */
    resetExamPaperForm() {
      this.examPaperForm = {
        paperName: '',
        difficultyLevel: null,
        durationMinutes: null,
        generateType: 2,
        paperDesc: '',
        chapterId: null,
        status: 0,
        questionCount: 0,
        totalScore: 0,
        paperCode: ''
      }
      this.currentExamPaperChapter = null
      if (this.$refs.examPaperForm) {
        this.$refs.examPaperForm.resetFields()
      }
    },
    /** 组卷按钮点击 */
    handlePaperCompose(paper, chapter) {
      this.currentComposePaper = paper
      this.currentComposeChapter = chapter
      this.paperComposeOpen = true
      this.loadPaperQuestions()
    },
    /** 加载试卷题目列表 */
    loadPaperQuestions() {
      if (!this.currentComposePaper || !this.currentComposePaper.id) {
        return
      }
      this.paperQuestionLoading = true
      listEduExamPaperQuestion({ paperId: this.currentComposePaper.id }).then(response => {
        const list = response.data || []
        // 后端已经按类型和排序排序，这里直接使用
        this.paperQuestionList = list
        this.calculateTotalScore()
        this.paperQuestionLoading = false
      }).catch(() => {
        this.paperQuestionLoading = false
      })
    },
    /** 根据类型获取试卷题目 */
    getPaperQuestionsByType(type) {
      return this.paperQuestionList.filter(q => q.questionType === type)
    },
    /** 计算试卷总分 */
    calculateTotalScore() {
      this.totalPaperScore = this.paperQuestionList.reduce((sum, q) => {
        return sum + (Number(q.questionScore) || 0)
      }, 0)
    },
    /** 添加题目到试卷 */
    handleAddQuestionToPaper() {
      if (!this.currentComposeChapter) {
        this.$message.warning('请先选择章节')
        return
      }
      this.addQuestionToPaperOpen = true
      this.chapterQuestionActiveTab = '1' // 默认选中第一个tab
      this.resetChapterQuestionQuery()
      this.handleChapterQuestionQuery()
    },
    /** 查询章节题目 */
    handleChapterQuestionQuery() {
      if (!this.currentComposeChapter) {
        return
      }
      this.chapterQuestionLoading = true
      // 根据当前选中的tab设置题目类型
      const currentQuestionType = parseInt(this.chapterQuestionActiveTab)
      const params = {
        current: this.chapterQuestionQueryParams.current,
        size: this.chapterQuestionQueryParams.size,
        entity: {
          chapterId: this.currentComposeChapter.id,
          questionTitle: this.chapterQuestionQueryParams.questionTitle || null,
          questionType: currentQuestionType, // 根据tab设置题目类型
          difficultyLevel: this.chapterQuestionQueryParams.difficultyLevel || null
        }
      }
      pageEduQuestionBank(params.current, params.size, params.entity).then(response => {
        const pageData = response.data
        this.chapterQuestionList = pageData.records || []
        this.chapterQuestionTotal = Number(pageData.total) || 0
        this.chapterQuestionLoading = false
      }).catch(() => {
        this.chapterQuestionLoading = false
      })
    },
    /** 根据类型获取章节题目 */
    getChapterQuestionsByType(type) {
      const typeNum = typeof type === 'string' ? parseInt(type) : type
      return this.chapterQuestionList.filter(q => {
        const qType = typeof q.questionType === 'string' ? parseInt(q.questionType) : q.questionType
        return qType === typeNum
      })
    },
    /** 题目类型tab切换 */
    handleChapterQuestionTabClick(tab) {
      this.chapterQuestionActiveTab = tab.name
      // 切换tab时重置页码并重新查询
      this.chapterQuestionQueryParams.current = 1
      this.handleChapterQuestionQuery()
    },
    /** 分页大小变化 */
    handleChapterQuestionSizeChange(size) {
      this.chapterQuestionQueryParams.size = size
      this.chapterQuestionQueryParams.current = 1
      this.handleChapterQuestionQuery()
    },
    /** 当前页变化 */
    handleChapterQuestionCurrentChange(current) {
      this.chapterQuestionQueryParams.current = current
      this.handleChapterQuestionQuery()
    },
    /** 重置章节题目查询 */
    resetChapterQuestionQuery() {
      this.chapterQuestionQueryParams = {
        current: 1,
        size: 10,
        questionTitle: null,
        questionType: null,
        difficultyLevel: null
      }
      this.selectedChapterQuestions = []
    },
    /** 检查题目是否可选（已选中的不能选） */
    checkQuestionSelectable(row) {
      // 检查该题目是否已经在试卷中
      return !this.paperQuestionList.some(pq => pq.questionId === row.id)
    },
    /** 章节题目选择变化 */
    handleChapterQuestionSelectionChange(selection) {
      this.selectedChapterQuestions = selection
    },
    /** 确认添加题目到试卷 */
    confirmAddQuestionsToPaper() {
      if (!this.selectedChapterQuestions || this.selectedChapterQuestions.length === 0) {
        this.$message.warning('请至少选择一个题目')
        return
      }
      // 检查是否超过20个
      if (this.paperQuestionList.length + this.selectedChapterQuestions.length > 20) {
        this.$message.warning('试卷题目总数不能超过20个')
        return
      }
      // 添加到试卷题目列表（暂存，未保存到数据库）
      const maxSortOrder = this.paperQuestionList.length > 0
        ? Math.max(...this.paperQuestionList.map(q => q.sortOrder || 0))
        : 0
      this.selectedChapterQuestions.forEach((q, index) => {
        const paperQuestion = {
          questionId: q.id,
          questionTitle: q.questionTitle,
          questionType: q.questionType,
          difficultyLevel: q.difficultyLevel,
          questionScore: q.score || 5, // 默认分值
          sortOrder: maxSortOrder + index + 1
        }
        this.paperQuestionList.push(paperQuestion)
      })
      // 按类型和排序重新排序
      this.paperQuestionList.sort((a, b) => {
        if (a.questionType !== b.questionType) {
          return a.questionType - b.questionType
        }
        return (a.sortOrder || 0) - (b.sortOrder || 0)
      })
      this.calculateTotalScore()
      // 同步更新试卷的题目数量和总分
      this.updatePaperCountAndScore()
      this.addQuestionToPaperOpen = false
      this.$message.success('题目已添加到试卷')
    },
    /** 从试卷中移除题目 */
    handleRemoveQuestionFromPaper(row) {
      this.$confirm('确定要从试卷中移除该题目吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const index = this.paperQuestionList.findIndex(q => q.questionId === row.questionId)
        if (index > -1) {
          this.paperQuestionList.splice(index, 1)
          this.calculateTotalScore()
          // 同步更新试卷的题目数量和总分
          this.updatePaperCountAndScore()
          this.$message.success('题目已移除')
        }
      })
    },
    /** 题目分值变化 */
    handleQuestionScoreChange() {
      this.calculateTotalScore()
      // 同步更新试卷的总分
      this.updatePaperCountAndScore()
    },
    /** 题目排序变化 */
    handleQuestionSortChange() {
      // 重新排序
      this.paperQuestionList.sort((a, b) => {
        if (a.questionType !== b.questionType) {
          return a.questionType - b.questionType
        }
        return (a.sortOrder || 0) - (b.sortOrder || 0)
      })
    },
    /** 保存试卷题目 */
    savePaperQuestions() {
      if (!this.currentComposePaper || !this.currentComposePaper.id) {
        return
      }
      // 先删除所有现有题目
      const existingQuestions = this.paperQuestionList.filter(q => q.id)
      if (existingQuestions.length > 0) {
        const ids = existingQuestions.map(q => q.id)
        deleteBatchEduExamPaperQuestion(ids).then(() => {
          this.saveNewPaperQuestions()
        }).catch(() => {
          this.$message.error('删除旧题目失败')
        })
      } else {
        this.saveNewPaperQuestions()
      }
    },
    /** 更新试卷的题目数量和总分（实时更新，不保存到数据库） */
    updatePaperCountAndScore() {
      if (!this.currentComposePaper || !this.currentComposePaper.id) {
        return
      }
      const questionCount = this.paperQuestionList.length
      const totalScore = this.totalPaperScore
      // 实时更新试卷信息（不等待保存）
      updateEduChapterExamPaper({
        id: this.currentComposePaper.id,
        questionCount: questionCount,
        totalScore: totalScore
      }).then(() => {
        // 更新当前试卷对象
        this.currentComposePaper.questionCount = questionCount
        this.currentComposePaper.totalScore = totalScore
      }).catch(() => {
        // 静默失败，不影响用户体验
        console.error('更新试卷信息失败')
      })
    },
    /** 保存新题目 */
    saveNewPaperQuestions() {
      if (this.paperQuestionList.length === 0) {
        this.$message.warning('请至少添加一个题目')
        return
      }
      // 批量保存新题目
      const promises = this.paperQuestionList.map((q, index) => {
        const paperQuestion = {
          paperId: this.currentComposePaper.id,
          questionId: q.questionId,
          questionScore: q.questionScore || 5,
          sortOrder: q.sortOrder || (index + 1)
        }
        return saveEduExamPaperQuestion(paperQuestion)
      })
      Promise.all(promises).then(() => {
        // 更新试卷的题目数量和总分
        const questionCount = this.paperQuestionList.length
        const totalScore = this.totalPaperScore
        updateEduChapterExamPaper({
          id: this.currentComposePaper.id,
          questionCount: questionCount,
          totalScore: totalScore
        }).then(() => {
          this.$message.success('保存成功')
          this.paperComposeOpen = false
          // 刷新章节试卷列表
          if (this.currentComposeChapter) {
            this.loadChapterExamPapers(this.currentComposeChapter)
          }
        }).catch(() => {
          this.$message.error('更新试卷信息失败')
        })
      }).catch(() => {
        this.$message.error('保存题目失败')
      })
    },
    /** AI生成题目（每次重新生成并覆盖当前试卷题目） */
    handleAiGenerateQuestions() {
      if (!this.currentComposePaper || !this.currentComposeChapter) {
        this.$message.warning('试卷信息不完整')
        return
      }
      const courseId = this.selectedCourse ? this.selectedCourse.id : null
      if (!courseId) {
        this.$message.warning('无法获取课程信息')
        return
      }
      const tip = this.paperQuestionList.length > 0
        ? '将重新生成题目并覆盖当前试卷中的题目，是否继续？'
        : '确定要使用AI生成题目吗？'
      this.$confirm(tip, 'AI组卷', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        this.aiGenerating = true
        listEduCourseChapter({ courseId: courseId }).then(chapterResponse => {
          const chapters = chapterResponse.data || []
          if (chapters.length === 0) {
            this.$message.warning('该课程下没有章节')
            this.aiGenerating = false
            return
          }
          const chapterIds = chapters.map(c => c.id)
          const questionPromises = chapterIds.map(chapterId => {
            return pageEduQuestionBank(1, 1000, { chapterId: chapterId })
          })
          Promise.all(questionPromises).then(questionResponses => {
            const allQuestions = []
            questionResponses.forEach(response => {
              const pageData = response.data
              if (pageData && pageData.records) {
                allQuestions.push(...pageData.records)
              }
            })
            if (allQuestions.length === 0) {
              this.$message.warning('该课程下没有可用题目')
              this.aiGenerating = false
              return
            }
            // 每次生成最多20道，用于覆盖当前试卷
            const requestCount = Math.min(20, allQuestions.length)
            const aiRequest = {
              paperId: this.currentComposePaper.id,
              difficultyLevel: this.currentComposePaper.difficultyLevel,
              questionCount: requestCount,
              availableQuestions: allQuestions.map(q => ({
                id: q.id,
                title: q.questionTitle,
                type: q.questionType,
                difficulty: q.difficultyLevel,
                score: q.score
              }))
            }
            aiGeneratePaperQuestions(aiRequest).then(response => {
              const selectedQuestionIds = response.data || []
              if (selectedQuestionIds.length === 0) {
                this.$message.warning('AI未生成任何题目')
                this.aiGenerating = false
                return
              }
              // 用新生成的题目覆盖当前试卷题目（不追加）
              const selectedQuestions = allQuestions.filter(q => selectedQuestionIds.includes(q.id))
              this.paperQuestionList = selectedQuestions.map((q, index) => ({
                questionId: q.id,
                questionTitle: q.questionTitle,
                questionType: q.questionType,
                difficultyLevel: q.difficultyLevel,
                questionScore: q.score || 5,
                sortOrder: index + 1
              }))
              // 按题型顺序（单选、多选、填空、判断、主观）及排序
              this.paperQuestionList.sort((a, b) => {
                if (a.questionType !== b.questionType) {
                  return a.questionType - b.questionType
                }
                return (a.sortOrder || 0) - (b.sortOrder || 0)
              })
              this.paperQuestionList.forEach((q, i) => { q.sortOrder = i + 1 })
              this.calculateTotalScore()
              this.updatePaperCountAndScore()
              this.$message.success(`已重新生成并覆盖原题目，共 ${this.paperQuestionList.length} 道。请保存以生效。`)
              this.aiGenerating = false
            }).catch(error => {
              console.error('AI生成失败:', error)
              this.$message.error('AI生成失败：' + (error.message || '未知错误'))
              this.aiGenerating = false
            })
          }).catch(error => {
            console.error('获取题目列表失败:', error)
            this.$message.error('获取题目列表失败')
            this.aiGenerating = false
          })
        }).catch(error => {
          console.error('获取章节列表失败:', error)
          this.$message.error('获取章节列表失败')
          this.aiGenerating = false
        })
      }).catch(() => {
        this.aiGenerating = false
      })
    }
  }
}
</script>

<style scoped lang="scss">
.course-detail-container {
  height: calc(100vh - 84px);
  overflow: hidden;
}

.course-detail-layout {
  height: 100%;

  .left-panel,
  .right-panel {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
}

// 左侧课程列表样式
.left-panel {
  border-right: 1px solid #e4e7ed;
  background: #f5f7fa;

  .panel-header {
    padding: 15px;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;

    h3 {
      margin: 0 0 10px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .course-list-wrapper {
    flex: 1;
    overflow-y: auto;
    padding: 10px;

    .course-card {
      background: #fff;
      border: 2px solid #e4e7ed;
      border-radius: 8px;
      padding: 15px;
      margin-bottom: 10px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: #409EFF;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      }

      &.active {
        border-color: #409EFF;
        background: #ecf5ff;
        box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.2);
      }

      .course-card-header {
        display: flex;
        align-items: center;
        margin-bottom: 10px;

        .course-cover {
          border-radius: 4px;
          margin-right: 12px;
        }

        .course-cover-placeholder {
          width: 80px;
          height: 60px;
          background: #f0f2f5;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;
          color: #909399;
          font-size: 24px;
        }

        .course-info {
          flex: 1;

          .course-name {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 8px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .course-meta {
            display: flex;
            align-items: center;
          }
        }
      }

      .course-card-footer {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #909399;
        padding-top: 10px;
        border-top: 1px solid #e4e7ed;

        .chapter-count,
        .teacher-name {
          display: flex;
          align-items: center;

          i {
            margin-right: 4px;
          }
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 40px 20px;
      color: #909399;

      i {
        font-size: 48px;
        margin-bottom: 10px;
      }

      p {
        margin: 0;
      }
    }
  }

  .course-pagination {
    padding: 10px;
    background: #fff;
    border-top: 1px solid #e4e7ed;
    text-align: center;
  }
}

// 右侧章节信息样式
.right-panel {
  background: #fff;

  .chapter-panel {
    height: 100%;
    display: flex;
    flex-direction: column;

    .panel-header {
      padding: 15px 20px;
      border-bottom: 1px solid #e4e7ed;
      display: flex;
      justify-content: space-between;
      align-items: center;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .chapter-search {
      padding: 15px 20px;
      background: #f5f7fa;
      border-bottom: 1px solid #e4e7ed;
    }

    .chapter-table-wrapper {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
    }

    .chapter-pagination {
      padding: 15px 20px;
      border-top: 1px solid #e4e7ed;
      text-align: right;
    }
  }

  .empty-selection {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #909399;

    i {
      font-size: 64px;
      margin-bottom: 20px;
    }

    p {
      margin: 0;
      font-size: 16px;
    }
  }
}

.content-cover-thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  vertical-align: middle;
}

.content-manage-container {
  .content-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}

::v-deep .content-manage-dialog {
  .el-dialog__body {
    padding: 20px;
    max-height: 70vh;
    overflow-y: auto;
  }
}

.question-manage-container {
  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .question-search {
    padding: 15px;
    background: #f5f7fa;
    border-radius: 4px;
    margin-bottom: 15px;
  }

  .question-tabs {
    margin-bottom: 15px;

    ::v-deep .el-tabs__header {
      margin: 0;
    }

    ::v-deep .el-tabs__item {
      font-size: 14px;
      padding: 0 20px;
      height: 40px;
      line-height: 40px;

      i {
        margin-right: 5px;
      }
    }
  }
}

::v-deep .question-manage-dialog {
  .el-dialog__body {
    padding: 20px;
    max-height: 75vh;
    overflow-y: auto;
  }
}

// 题目选项管理样式
.question-options-container {
  width: 100%;

  .question-option-item {
    margin-bottom: 15px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
    border: 1px solid #e4e7ed;

    .option-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10px;

      .option-label {
        font-weight: 600;
        color: #303133;
        font-size: 14px;
        min-width: 30px;
      }

      .el-checkbox {
        margin-left: 10px;
      }
    }

    &:last-child {
      margin-bottom: 0;
    }
  }

  .el-button {
    margin-top: 10px;
    padding-left: 0;
  }
}

// 试卷展开行样式 - 优化层级展示
.exam-paper-expand-content {
  position: relative;
  padding: 0;
  background: #fff;
  border-left: 3px solid #409EFF;
  margin-left: 20px;
  margin-right: 20px;
  margin-top: 10px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .expand-indicator {
    display: flex;
    align-items: center;
    padding: 8px 15px;
    background: linear-gradient(90deg, #e6f3ff 0%, #f0f9ff 100%);
    border-bottom: 1px solid #d9ecff;

    i {
      color: #409EFF;
      font-size: 14px;
      margin-right: 6px;
    }

    .expand-label {
      font-size: 13px;
      color: #409EFF;
      font-weight: 500;
    }
  }

  .expand-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 15px;
    background: #fafbfc;
    border-bottom: 1px solid #e4e7ed;

    .expand-title {
      display: flex;
      align-items: center;
      font-size: 14px;
      color: #606266;
      font-weight: 500;

      i {
        margin-right: 6px;
        color: #909399;
        font-size: 16px;
      }
    }
  }

  .expand-table-wrapper {
    padding: 15px;
    background: #fff;

    .nested-table {
      font-size: 13px;

      ::v-deep .el-table__header {
        th {
          background-color: #f5f7fa;
          color: #606266;
          font-weight: 500;
          padding: 8px 0;
        }
      }

      ::v-deep .el-table__body {
        tr {
          &:hover {
            background-color: #f5f7fa;
          }
        }

        td {
          padding: 8px 0;
        }
      }
    }

    .empty-paper-list {
      text-align: center;
      padding: 40px 20px;
      color: #909399;

      i {
        font-size: 48px;
        margin-bottom: 10px;
        display: block;
        color: #c0c4cc;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }
  }
}

.paper-compose-container {
  .compose-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #e4e7ed;

    .paper-info {
      flex: 1;

      h3 {
        margin: 0 0 10px 0;
        font-size: 18px;
        color: #303133;
      }

      .paper-meta {
        display: flex;
        align-items: center;
      }
    }

    .compose-actions {
      display: flex;
      gap: 10px;
    }
  }

  .paper-question-sections {
    margin-top: 20px;
  }
  .paper-question-section {
    margin-bottom: 24px;
    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 10px;
      padding-bottom: 6px;
      border-bottom: 1px solid #e4e7ed;
    }
  }
}

.add-question-container {
  .add-question-header {
    margin-bottom: 15px;
  }
}
</style>
