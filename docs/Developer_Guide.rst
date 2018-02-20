.. ===============LICENSE_START=======================================================
.. Acumos
.. ===================================================================================
.. Copyright (C) 2017-2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..  
..      http://creativecommons.org/licenses/by/4.0
..  
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================
=================
Developer's Guide
=================

Acumos ELK stack for log analytics
===================================
This guide that describes how to use the Acumos ELK Stack.

1. Introduction
---------------
One of the functions of Operations, Administration, and Management (OA&M) for the Acumos platform is to collect and correlate log files from the other platform components in order to support debugging, metrics, alarms, etc. for development and operations purposes. To this end, the OA&M component has defined a logging standard to be used by all of those components in order to support correlation. 

	1.1 What is Acumos ELK Stack?
		OAM are the processes, activities, tools, and standards involved with operating, administering, managing and maintaining any system. Collecting Logs of all the micro-services involved can reveal about the system that can mitigate potential risks. There are many ways we can collect logs that are generated via the micro-services. One of them is ELK stack. 
		ELK stands for, E- Elasticsearch L- Logstash K-Kibana		
        Note: Filebeats( a lightweight log shipping tools is also used to ship the logs to the servers)

	1.2 Target Users
		This guide is targeted towards the open source user community that who have access of super site admin.
  
	1.3 Assumptions
		All the module should follow Acumos Log Standards.

	1.4 ELK-Stack - Backend Architecture
		.. image:: images/elk_stack.png


2. ELK-Stack User Interface A Tour
----------------------------------
Site admin having access to ELK stack Login to dashboard:

.. image:: images/acumosSiteAdminUntitled.png

Kibana is an open source analytics and visualization platform designed to work with Elasticsearch. You use Kibana to search, view, and interact with data stored in Elasticsearch indices. You can easily perform advanced data analysis and visualize your data in a variety of charts, tables, and maps.
Kibana makes it easy to understand large volumes of data. Its simple, browser-based interface enables you to quickly create queries in real time.

For more details visit `Kibana User Guide <https://www.elastic.co/guide/en/kibana/current/index.html/>`_.


.. image:: images/loadingKibanaCapture.PNG
