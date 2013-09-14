
package org.jboss.as.controller.descriptions

object ModelDescriptionConstants {
  // KEEP THESE IN ALPHABETICAL ORDER!

  val ACCESS_TYPE = "access-type";
  val ADD = "add";
  val ADD_OPERATION = "add-operation";
  val ADDRESS = "address";
  val ALLOWED = "allowed";
  val ANY = "any";
  val ANY_ADDRESS = "any-address";
  val ANY_IPV4_ADDRESS = "any-ipv4-address";
  val ANY_IPV6_ADDRESS = "any-ipv6-address";
  val ATTRIBUTES = "attributes";
  val BYTES = "bytes";
  val CANCELLED = "cancelled";
  val CHILD_TYPE = "child-type";
  val CHILDREN = "children";
  val COMPOSITE = "composite";
  val CONCURRENT_GROUPS = "concurrent-groups";
  val CPU_AFFINITY = "cpu-affinity";
  val CRITERIA = "criteria";
  val COMPENSATING_OPERATION = "compensating-operation";
  val DEFAULT = "default";
  val DESCRIBE = "describe";
  val DEFAULT_INTERFACE = "default-interface";
  val DEPLOYMENT = "deployment";
  val DESCRIPTION = "description";
  val DOMAIN_CONTROLLER = "domain-controller";
  val DOMAIN_FAILURE_DESCRIPTION = "domain-failure-description";
  val DOMAIN_RESULTS = "domain-results";
  val EXTENSION = "extension";
  val FAILED = "failed";
  val FAILURE_DESCRIPTION = "failure-description";
  val FIXED_PORT = "fixed-port";
  val GRACEFUL_SHUTDOWN_TIMEOUT = "graceful-shutdown-timeout";
  val GROUP = "group";
  val HASH = "hash";
  val HEAD_COMMENT_ALLOWED = "head-comment-allowed";
  val HTTP_INTERFACE = "http-interface";
  val HOST = "host";
  val HOST_FAILURE_DESCRIPTION = "host-failure-description";
  val HOST_FAILURE_DESCRIPTIONS = "host-failure-descriptions";
  val IGNORED = "ignored";
  val INCLUDE = "include";
  val INCLUDES = "includes";
  val INCLUDE_RUNTIME = "include-runtime";
  val INHERITED = "inherited";
  val INET_ADDRESS = "inet-address";
  val INPUT_STREAM_INDEX = "input-stream-index";
  val INTERFACE = "interface";
  val IN_SERIES = "in-series";
  val JVM = "jvm";
  val JVM_TYPE = "type";
  val LOCAL = "local";
  val LOCALE = "locale";
  val MANAGEMENT_INTERFACES = "management-interfaces";
  val MASK = "mask";
  val MAX = "max";
  val MAX_FAILED_SERVERS = "max-failed-servers";
  val MAX_FAILURE_PERCENTAGE = "max-failure-percentage";
  val MAX_LENGTH = "max-length";
  val MAX_OCCURS = "max-occurs";
  val MAX_THREADS = "max-threads";
  val MIN = "min";
  val MIN_LENGTH = "min-length";
  val MIN_OCCURS = "min-occurs";
  val MIN_VALUE = "min-value";
  val MODEL_DESCRIPTION = "model-description";
  val MULTICAST_ADDRESS = "multicast-address";
  val MULTICAST_PORT = "multicast-port";
  val NAME = "name";
  val NAMESPACE = "namespace";
  val NAMESPACES = "namespaces";
  val NATIVE_INTERFACE = "native-interface";
  val NETWORK = "network";
  val NILLABLE = "nillable";
  val NOT = "not";
  val NOT_SET = "not-set";
  /** Use this as the standard operation name field in the operation *request* ModelNode */
  val OP = "operation";
  /** Use this standard operation address field in the operation *request* ModelNode */
  val OP_ADDR = "address";
  val OPERATIONS = "operations";
  /** Use this standard operation address field in the operation *description* ModelNode */
  val OPERATION_NAME = "operation-name";
  val OUTCOME = "outcome";
  val PATH = "path";
  val PORT = "port";
  val PORT_OFFSET = "port-offset";
  val PRIORITY = "priority";
  val PROFILE = "profile";
  val PROFILE_NAME = "profile-name";
  val PROXIES = "proxies";
  val READ_ATTRIBUTE_OPERATION = "read-attribute";
  val READ_CHILDREN_NAMES_OPERATION = "read-children-names";
  val READ_CHILDREN_TYPES_OPERATION = "read-children-types";
  val READ_CHILDREN_RESOURCES_OPERATION = "read-children-resources";
  val READ_CONFIG_AS_XML_OPERATION = "read-config-as-xml";
  val READ_OPERATION_DESCRIPTION_OPERATION = "read-operation-description";
  val READ_OPERATION_NAMES_OPERATION = "read-operation-names";
  val READ_RESOURCE_DESCRIPTION_OPERATION = "read-resource-description";
  val READ_RESOURCE_METRICS = "read-resource-metrics";
  val READ_RESOURCE_OPERATION = "read-resource";
  val RELATIVE_TO = "relative-to";
  val REMOVE = "remove";
  val REMOTE = "remote";
  val REMOVE_OPERATION = "remove-operation";
  val REPLY_PROPERTIES = "reply-properties";
  val REQUEST_PROPERTIES = "request-properties";
  val RECURSIVE = "recursive";
  val REQUIRED = "required";
  val RESPONSE = "response";
  val RESULT = "result";
  val ROLLBACK_ACROSS_GROUPS = "rollback-across-groups";
  val ROLLBACK_FAILURE_DESCRIPTION = "rollback-failure-description";
  val ROLLBACK_ON_RUNTIME_FAILURE = "rollback-on-runtime-failure";
  val ROLLED_BACK = "rolled-back";
  val ROLLING_TO_SERVERS = "rolling-to-servers";
  val ROLLOUT_PLAN = "rollout-plan";
  val RUNTIME_NAME = "runtime-name";
  val RUNNING_SERVER = "server";
  val SCHEMA_LOCATION = "schema-location";
  val SCHEMA_LOCATIONS = "schema-locations";
  val SERVER = "server";
  val SERVERS = "servers";
  val SERVER_CONFIG = "server-config";
  val SERVER_GROUP = "server-group";
  val SERVER_GROUPS = "server-groups";
  val SERVER_OPERATIONS = "server-operations";
  val SHUTDOWN = "shutdown";
  val SOCKET_BINDING = "socket-binding";
  val SOCKET_BINDING_GROUP = "socket-binding-group";
  val SOCKET_BINDING_GROUP_NAME = "socket-binding-group-name";
  val SOCKET_BINDING_PORT_OFFSET = "socket-binding-port-offset";
  val START = "start";
  val STEPS = "steps";

  val STORAGE = "storage";
  val SUBSYSTEM = "subsystem";
  val SUCCESS = "success";
  val SYSTEM_PROPERTY = "system-property";
  val SYSTEM_PROPERTIES = "system-properties";
  val TAIL_COMMENT_ALLOWED = "tail-comment-allowed";
  val TO_REPLACE = "to-replace";
  val TYPE = "type";
  val URL = "url";
  val VALUE = "value";
  val VALUE_TYPE = "value-type";
  val WRITE_ATTRIBUTE_OPERATION = "write-attribute";
  val OPERATION_HEADERS = "operation-headers";
  val ALLOW_RESOURCE_SERVICE_RESTART = "allow-resource-service-restart";
}

