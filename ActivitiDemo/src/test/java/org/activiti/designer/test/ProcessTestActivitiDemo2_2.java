package org.activiti.designer.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import delegate.SenderDelegate;
import delegate.SenderMgrDelegate;

public class ProcessTestActivitiDemo2_2 {

	private String filename = "src\\main\\resources\\diagrams\\ActivitiDemo2_2.bpmn";

	// Activiti Service
	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	public RepositoryService repositoryService = processEngine.getRepositoryService();
	public RuntimeService runtimeService = processEngine.getRuntimeService();
	public TaskService taskService = processEngine.getTaskService();
	public HistoryService historyService = processEngine.getHistoryService();
	
	
	private static int diagramSequence = 1;

	@Test
	public void startProcess() throws Exception {
		/*
		佈署流程圖(.bpmn)
		*/
		repositoryService.createDeployment().addInputStream("ActivitiDemo2_2.bpmn20.xml",
				new FileInputStream(filename)).deploy();
		// ----------------佈署流程圖(.bpmn) end---------------		
		
		/*
		1.start process (draft)
		*/
		Map<String, Object> variableMap = new HashMap<String, Object>(); // 流程變數
		variableMap.put("senderDelegate", new SenderDelegate());	//抓經辦的Delegate
		// 產生流程
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ActivitiDemo2_2", variableMap);
		//產出流程中圖片
		getProcessDiagram(processInstance.getId(), "D:\\activitiDemoDiagram\\" + processInstance.getId() + "\\");
		// ----------------start process end----------------
		
		/*
		2.next process (Under Review)
		*/
		// 取得目前所在關卡
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		variableMap.put("senderMgrDelegate", new SenderMgrDelegate()); //抓申請者主管的Delegate
		variableMap.put("condition", "submit"); 	
		variableMap.put("owner", "owner"); 	
		task = completeTask(task, variableMap); //送到下一關
		// ----------------end----------------
		
		/*
		3.next process 
		產生兩個會辦Task
		Credit Assigning 
		Legal Assigning 
		*/
		// 取得目前所在關卡
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		variableMap = new HashMap<String, Object>();
		variableMap.put("creditOwner", "creditSender"); 
		variableMap.put("legalOwner", "legalSender");	
		variableMap.put("accountingOwner", null);	
		variableMap.put("condition", "submit"); 	
		TaskQuery taskQuery = completeTaskCountersign(task, variableMap); 
		long taskcount = taskQuery.count();	//目前Task數量
		// ----------------end----------------
		
		/*
		4.next process 
		Credit Assigning -> Credit Under Review 
		Legal Assigning
		*/
		// 取得task繼續流程
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list().get(0);
		variableMap = new HashMap<String, Object>();
		variableMap.put("condition", "assign"); 
		variableMap.put("owner", "creditowner");
		taskQuery = completeTaskCountersign(task, variableMap); 
		//taskcount = taskQuery.count();	//目前Task數量
		List<String> aaa = runtimeService.getActiveActivityIds(processInstance.getId());
		// ----------------end----------------
		
		
		/*
		5.next process 
		Credit Assigning -> Credit Under Review 
		Legal Assigning -> Legal Under Review 
		*/
		// 取得task繼續流程
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list().get(0);
		variableMap = new HashMap<String, Object>();
		variableMap.put("condition", "assign"); 
		taskQuery = completeTaskCountersign(task, variableMap); 
		aaa = runtimeService.getActiveActivityIds(processInstance.getId());
		// ----------------end----------------
		


		
		/*
		6.next process 
		Credit Assigning -> Credit Under Review  -> Credit Under Review  
		Legal Assigning -> Legal Under Review
		*/
		// 取得task繼續流程
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list().get(0);
		variableMap = new HashMap<String, Object>();
		variableMap.put("condition", "submit"); 
		variableMap.put("senderMgrDelegate", new SenderMgrDelegate()); //抓申請者主管的Delegate
		taskQuery = completeTaskCountersign(task, variableMap); 
		// ----------------end----------------
		
		/*
		7.next process 
		Credit Assigning -> Credit Under Review  -> Credit Under Review  -> Task End 
		Legal Assigning -> Legal Under Review
		*/
		// 取得task繼續流程
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list().get(1);
		variableMap = new HashMap<String, Object>();
		variableMap.put("condition", "submit"); 
		taskQuery = completeTaskCountersign(task, variableMap); 
		// ----------------end----------------
		
		/*
		8.next process 
		Credit Assigning -> Credit Under Review  -> Credit Under Review  -> Task End 
		Legal Assigning -> Legal Under Review   -> SignalCatchEvent 
		*/
		// 取得task繼續流程
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		variableMap = new HashMap<String, Object>();
		variableMap.put("condition", "submit"); 
		variableMap.put("senderMgrDelegate", new SenderMgrDelegate()); //抓申請者主管的Delegate
		taskQuery = completeTaskCountersign(task, variableMap); 
		// ----------------end----------------
		
		/*
		9.trigger signal
		 */
		runtimeService.signalEventReceived("returnInSection", variableMap);
		//產出流程中圖片
		getProcessDiagram(processInstance.getId(), "D:\\activitiDemoDiagram\\" + processInstance.getId() + "\\");
		// ----------------end----------------
			
		/*
		10.next process final Task
		*/
		// 取得task繼續流程
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		variableMap = new HashMap<String, Object>();
		taskQuery = completeTaskCountersign(task, variableMap); 
		// ----------------end----------------
		
		
		/*
		11.finish process
		*/
		// 取得task繼續流程
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		variableMap = new HashMap<String, Object>();
		taskQuery = completeTaskCountersign(task, variableMap); 
		// ----------------end----------------
		
		
		// 查詢已完成流程id
		List<HistoricProcessInstance> finishedProcessInstList = historyService.createHistoricProcessInstanceQuery()
				.finished().list();
		assertNotNull(finishedProcessInstList);
		System.out.println(String.format("流程已結束(id=%s)", finishedProcessInstList.get(0).getId()));
	}
	
	/**
	 * Get Process instance diagram
	 * @param processInstanceId 流程id
	 * @param path 檔案路徑
	 */
	public void getProcessDiagram(String processInstanceId, String path) {
	    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
	            .processInstanceId(processInstanceId).singleResult();

	    // 有抓到processId才產圖
	    if (processInstance != null) {
	        // get process model
	        BpmnModel model = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

	        if (model != null && model.getLocationMap().size() > 0) {
	            ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
	            InputStream stream = generator.generateDiagram(model, runtimeService.getActiveActivityIds(processInstanceId));
	            
	            // write to file
	            String fileName = diagramSequence + "." + System.currentTimeMillis();
	            diagramSequence++;
	            try {
					FileUtils.copyInputStreamToFile(stream, new File(path + fileName + ".svg"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
	}
	
	/**
	 * 關卡簽核
	 * @param task 待簽核關卡
	 * @param properties 關卡變數
	 * @return 新的待辦關卡
	 */
	private Task completeTask(Task task, Map<String, Object> properties) {
		taskService.complete(task.getId(), properties);
		//產出流程中圖片
		getProcessDiagram(task.getProcessInstanceId(), "D:\\activitiDemoDiagram\\" + task.getProcessInstanceId() + "\\");
		return taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
	}
	
	/**
	 * 關卡簽核
	 * @param task 待簽核關卡
	 * @param properties 關卡變數
	 * @return 新的待辦關卡(多筆)
	 */
	private TaskQuery completeTaskCountersign(Task task, Map<String, Object> properties) {
		taskService.complete(task.getId(), properties);
		//產出流程中圖片
		getProcessDiagram(task.getProcessInstanceId(), "D:\\activitiDemoDiagram\\" + task.getProcessInstanceId() + "\\");
		return taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId());
	}
	
}