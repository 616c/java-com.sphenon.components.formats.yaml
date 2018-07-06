package com.sphenon.formats.yaml;

/****************************************************************************
  Copyright 2001-2018 Sphenon GmbH

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations
  under the License.
*****************************************************************************/

import com.sphenon.basics.context.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.configuration.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;
import com.sphenon.basics.many.*;

import com.sphenon.formats.yaml.returncodes.*;

import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.*;
import java.io.*;

public class YAMLNode implements GenericIterable<YAMLNode> {
    static final public Class _class = YAMLNode.class;

    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(_class); };

    protected CallContext creation_context;
    protected Vector<Object>   nodes;
    protected Object           first_node;
    protected Vector<YAMLNode> yaml_nodes;
    protected String name;

    static protected Object parseYAML(CallContext context, String yaml_string) throws InvalidYAML {
        Yaml yaml = new Yaml();
        // Yaml yaml = new Yaml(new SafeConstructor()); // SafeConstructor limits to standard java objects
        try {
            return yaml.load(yaml_string);
        } catch (Exception e) {
            InvalidYAML.createAndThrow(context, e, "Cannot parse YAML '%(yaml_string)'", "yaml_string", yaml_string);
            throw (InvalidYAML) null; // compiler insists
        }
    }

    static protected Object parseYAML(CallContext context, InputStream yaml_stream) throws InvalidYAML {
        Yaml yaml = new Yaml();
        // Yaml yaml = new Yaml(new SafeConstructor()); // SafeConstructor limits to standard java objects
        try {
            return yaml.load(yaml_stream);
        } catch (Exception e) {
            InvalidYAML.createAndThrow(context, e, "Cannot parse YAML stream");
            throw (InvalidYAML) null; // compiler insists
        }
    }

    static protected Vector<Object> makeVector(Object node) {
        Vector<Object> vector = new Vector<Object>();
        vector.add(node);
        return vector;
    }

    public YAMLNode (CallContext context) {
        this(context, (Vector<Object>) null, null);
    }

    public YAMLNode (CallContext context, Object node) {
        this(context, makeVector(node), null);
    }

    public YAMLNode (CallContext context, Object node, String name) {
        this(context, makeVector(node), name);
    }

    public YAMLNode (CallContext context, Vector<Object> nodes) {
        this(context, nodes, null);
    }

    public YAMLNode (CallContext context, Vector<Object> nodes, String name) {
        this.creation_context = context;
        this.nodes = nodes;
        this.name = name;
        this.first_node = (this.nodes != null && this.nodes.size() >= 1 ? this.nodes.get(0) : null);
    }

    // static public YAMLNode createYAMLNode(CallContext context, TreeLeaf tree_leaf) throws InvalidYAML {
    //     Data_MediaObject data = ((Data_MediaObject)(((NodeContent_Data)(tree_leaf.getContent(context))).getData(context)));
    //     return createYAMLNode(context, data.getStream(context), data.getDispositionFilename(context));
    // }

    static public YAMLNode createYAMLNode(CallContext context, Object node) throws InvalidYAML {
        return new YAMLNode(context, node);
    }

    static public YAMLNode createYAMLNode(CallContext context, String yaml_string) throws InvalidYAML {
        return new YAMLNode(context, parseYAML(context, yaml_string));
    }

    static public YAMLNode createYAMLNode(CallContext context, InputStream input_stream) throws InvalidYAML {
        YAMLNode yaml_node = new YAMLNode(context, parseYAML(context, input_stream));
        try {
            input_stream.close();
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Could not close stream after parsing");
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }
        return yaml_node;
    }

    static public YAMLNode createYAMLNode(CallContext context, File file) throws InvalidYAML {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException fnfe) {
            CustomaryContext.create(Context.create(context)).throwPreConditionViolation(context, fnfe, "File '%(file)' does not exist (while creating YAML node)", "file", file.getPath());
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }
        YAMLNode yaml_node = createYAMLNode(context, new BufferedInputStream(fis) /* , file.getPath() */);
        try {
            fis.close();
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Could not close stream after parsing");
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }
        return yaml_node;
    }

    public Vector<YAMLNode> getNodes(CallContext context) {
        if (this.yaml_nodes == null) {
            this.yaml_nodes = new Vector<YAMLNode>();
            for (Object node : this.nodes) {
                this.yaml_nodes.add(new YAMLNode(context, node));
            }
        }
        return this.yaml_nodes;
    }

    public Vector<Object> getYamlNodes(CallContext context) {
        return this.nodes;
    }

    public Object getFirstNode(CallContext context) {
        return this.first_node;
    }

    public String getName(CallContext context) {
        if (this.name == null) {
            // [ToDo:YAML] !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // if (this.first_node.isObject()) {
            //     this.name = this.first_node.get("@Name").asText();
            // }
            if (this.name == null) {
                this.name = "";
            }
        }
        return this.name;
    }

    // [ToDo:YAML] !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // public String getAttribute(CallContext context, String name) {
    //     return this.first_node.get(name).asText();
    // }

    public boolean exists(CallContext context) {
        return this.first_node != null;
    }

    // [ToDo:YAML] !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // public String toString (CallContext context) {
    //     return this.first_node.asText();
    // }

    // [ToDo:YAML] !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // public String toString () {
    //     return this.first_node.asText();
    // }

    // public String serialise(CallContext context) {
    //     return YAMLUtil.serialise(context, this.nodes);
    // }

    // public String serialise(CallContext context, Writer writer) {
    //     return YAMLUtil.serialise(context, this.nodes, writer);
    // }

    // public String serialise(CallContext context, TreeLeaf tree_leaf) {
    //     return YAMLUtil.serialise(context, this.nodes, tree_leaf);
    // }

    // public String serialise(CallContext context, LSOutput ls_output) {
    //     return YAMLUtil.serialise(context, this.nodes, ls_output);
    // }

    // public String serialiseContent(CallContext context) {
    //     return YAMLUtil.serialiseContent(context, this.nodes);
    // }

    // public String serialiseContent(CallContext context, Writer writer) {
    //     return YAMLUtil.serialiseContent(context, this.nodes, writer);
    // }

    // public String serialiseContent(CallContext context, TreeLeaf tree_leaf) {
    //     return YAMLUtil.serialiseContent(context, this.nodes, tree_leaf);
    // }

    // public String serialiseContent(CallContext context, LSOutput ls_output) {
    //     return YAMLUtil.serialiseContent(context, this.nodes, ls_output);
    // }

    // protected void processTextNode(CallContext context, Node node, StringBuilder sb) {
    //     if (    node.getNodeType() == Node.TEXT_NODE
    //          || node.getNodeType() == Node.CDATA_SECTION_NODE
    //        ) {
    //         sb.append(((Text) node).getData());
    //         return;
    //     }
    //     CustomaryContext.create((Context)context).throwConfigurationError(context, "YAMLNode to render to text does contain DOM node not of type 'Text' (node '%(content)')", "content", this.toString(context));
    //     throw (ExceptionConfigurationError) null; // compiler insists
    // }

    // public String toText (CallContext context) {
    //     if (this.nodes == null) { return ""; }
    //     StringBuilder sb = new StringBuilder();
    //     for (Node node : nodes) {
    //         if (node.getNodeType() == Node.ELEMENT_NODE) {
    //             Node child = node.getFirstChild();
    //             while (child != null) {
    //                 processTextNode(context, child, sb);
    //                 child = child.getNextSibling();
    //             }
    //         } else {
    //             processTextNode(context, node, sb);
    //         }
    //     }
    //     return sb.toString();
    // }

    /*
      Baustelle...
      haut so alles noch nicht hin; wegen field names,
      die müßten ja auch mitgespeichert werden, also
      parallel zu nodes array noch ein names_array
      oder so
     
    public YAMLNode getChilds(CallContext context) {
        Vector<YamlNode> result_nodes = new Vector<Node>();
        
        int i=0;
        for (YamlNode node : this.nodes) {

            if (node.isObject()) {
                for (String field : node.fieldsNames()) {
                    YamlNode child = node.get(field);
                    result_nodes.add(result.item(n));
                }
            }
            if (node.isArray()) {
            }
        }

        return new YAMLNode(context, result_nodes);
    }
    */

    // /**
    //   @param filter they match attributes if name is just name; yaml:nsuri and yaml:name as names
    //                 match the namespace uri and name
    // */
    // public YAMLNode getChildElementsByRegExp(CallContext context, NamedRegularExpressionFilter... filters) {
    //     Vector<Node> result_nodes = new Vector<Node>();
        
    //     for (Node node : this.nodes) {

    //         NodeList result = node.getChildNodes();

    //         NODES: for (int n=0; n<result.getLength(); n++) {
    //             Node child = result.item(n);
    //             if (child.getNodeType() == Node.ELEMENT_NODE) {                    
    //                 if (filters != null && filters.length != 0) {
    //                     for (NamedRegularExpressionFilter nref : filters) {
    //                         String name = nref.getName(context);
    //                         if (nref.matches(context, 
    //                                          name.equals("yaml:nsuri") ? child.getNamespaceURI() :
    //                                          name.equals("yaml:name") ? child.getNodeName() :
    //                                          ((Element)child).getAttribute(name)
    //                                         ) == false) {
    //                             continue NODES;
    //                         }
    //                     }
    //                 }
    //                 result_nodes.add(child);
    //             }
    //         }
    //     }

    //     return new YAMLNode(context, result_nodes);
    // }

    // public YAMLNode getChildElementsByFilters(CallContext context, NamedRegularExpressionFilter[]... filters) {
    //     YAMLNode current = this;
    //     if (filters != null) {
    //         for (NamedRegularExpressionFilter[] filter : filters) {
    //             current = current.getChildElementsByRegExp(context, filter);
    //         }
    //     }
    //     return current;
    // }

    // public boolean isText(CallContext context) {
    //     if (nodes != null && nodes.size() == 1) {
    //         short nt = nodes.get(0).getNodeType();
    //         return (    nt == Node.CDATA_SECTION_NODE
    //                  || nt == Node.TEXT_NODE
    //                );
    //     }
    //     return false;
    // }

    // public boolean isElement(CallContext context) {
    //     if (nodes != null && nodes.size() == 1) {
    //         short nt = nodes.get(0).getNodeType();
    //         return (    nt == Node.ELEMENT_NODE
    //                );
    //     }
    //     return false;
    // }

    // public boolean isComment(CallContext context) {
    //     if (nodes != null && nodes.size() == 1) {
    //         short nt = nodes.get(0).getNodeType();
    //         return (    nt == Node.COMMENT_NODE
    //                );
    //     }
    //     return false;
    // }

    // public boolean isDocument(CallContext context) {
    //     if (nodes != null && nodes.size() == 1) {
    //         short nt = nodes.get(0).getNodeType();
    //         return (    nt == Node.DOCUMENT_NODE
    //                );
    //     }
    //     return false;
    // }

    // public boolean isDocumentType(CallContext context) {
    //     if (nodes != null && nodes.size() == 1) {
    //         short nt = nodes.get(0).getNodeType();
    //         return (    nt == Node.DOCUMENT_TYPE_NODE
    //                );
    //     }
    //     return false;
    // }

    // public boolean isProcessingInstruction(CallContext context) {
    //     if (nodes != null && nodes.size() == 1) {
    //         short nt = nodes.get(0).getNodeType();
    //         return (    nt == Node.PROCESSING_INSTRUCTION_NODE
    //                );
    //     }
    //     return false;
    // }

    // public String getNodeType(CallContext context) {
    //     String result = "";
    //     if (nodes != null) {
    //         for (Node node : nodes) {
    //             if (result != null && result.length() != 0) {
    //                 result += ",";
    //             }
    //             switch (node.getNodeType()) {
    //                 case Node.CDATA_SECTION_NODE :
    //                     result += "CDATA_SECTION_NODE"; break;
    //                 case Node.TEXT_NODE :
    //                     result += "TEXT_NODE"; break;
    //                 case Node.ELEMENT_NODE :
    //                     result += "ELEMENT_NODE"; break;
    //                 case Node.COMMENT_NODE :
    //                     result += "COMMENT_NODE"; break;
    //                 case Node.ATTRIBUTE_NODE  :
    //                     result += "ATTRIBUTE_NODE"; break;
    //                 case Node.DOCUMENT_FRAGMENT_NODE  :
    //                     result += "DOCUMENT_FRAGMENT_NODE"; break;
    //                 case Node.DOCUMENT_NODE  :
    //                     result += "DOCUMENT_NODE"; break;
    //                 case Node.DOCUMENT_TYPE_NODE  :
    //                     result += "DOCUMENT_TYPE_NODE"; break;
    //                 case Node.ENTITY_NODE  :
    //                     result += "ENTITY_NODE"; break;
    //                 case Node.ENTITY_REFERENCE_NODE  :
    //                     result += "ENTITY_REFERENCE_NODE"; break;
    //                 case Node.NOTATION_NODE  :
    //                     result += "NOTATION_NODE"; break;
    //                 case Node.PROCESSING_INSTRUCTION_NODE  :
    //                     result += "PROCESSING_INSTRUCTION_NODE"; break;
    //                 default :
    //                     result += "???"; break;
    //             }
    //         }
    //     }
    //     return result;
    // }

    // public String getNamespace(CallContext context) {
    //     return (nodes != null && nodes.size() == 1 ? nodes.get(0).getNamespaceURI() : null);
    // }

    // protected class MyNamespaceContext implements javax.yaml.namespace.NamespaceContext {
    //     protected Map<String,String> namespaces;
    //     public MyNamespaceContext(CallContext context, Map<String,String> namespaces) {
    //         this.namespaces = namespaces;
    //     }
    //     public String getNamespaceURI(String prefix) {
    //         if (prefix == null) throw new NullPointerException("Null prefix");
    //         else if ("pre".equals(prefix)) return "http://www.example.org/books";
    //         else if ("yaml".equals(prefix)) return YAMLConstants.YAML_NS_URI;
    //         String nsuri = namespaces == null ? null : namespaces.get(prefix);
    //         if (nsuri != null) { return nsuri; }
    //         return YAMLConstants.NULL_NS_URI;
    //     }

    //     // This method isn't necessary for XPath processing.
    //     public String getPrefix(String uri) {
    //         throw new UnsupportedOperationException();
    //     }

    //     // This method isn't necessary for XPath processing either.
    //     public Iterator getPrefixes(String uri) {
    //         throw new UnsupportedOperationException();
    //     }
    // }
    
    // static protected XPathFactory xpath_factory;

    // public YAMLNode resolveXPath(CallContext context, String xpath) {
    //     return resolveXPath(context, xpath, null);
    // }

    // public YAMLNode resolveXPath(CallContext context, String xpath, Map<String,String> namespaces) {
    //     if (xpath == null || xpath.length() == 0) { return this; }

    //     if (xpath_factory == null) {
    //         xpath_factory = XPathFactory.newInstance();
    //     }
    //     XPath xp = xpath_factory.newXPath();
    //     xp.setNamespaceContext(new MyNamespaceContext(context, namespaces));
    //     XPathExpression xpe = null;
    //     try {
    //         xpe = xp.compile(xpath);
    //     } catch (XPathExpressionException xpee) {
    //         CustomaryContext.create((Context)context).throwConfigurationError(context, xpee, "Could not compile XPath '%(xpath)'", "xpath", xpath);
    //         throw (ExceptionConfigurationError) null; // compiler insists
    //     }

    //     // XPathEvaluator evaluator = new XPathEvaluatorImpl(this.getOwnerDocument(context));

    //     Vector<Node> result_nodes = new Vector<Node>();
        
    //     for (Node node : this.nodes) {
    //         // XPathResult result = (XPathResult) evaluator.evaluate(xpath, node, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
    //         // Node result_node;
    //         // while ((result_node = result.iterateNext()) != null) {
    //         //     result_nodes.add(result_node);
    //         // }

    //         NodeList result = null;
    //         try {
    //             result = (org.w3c.dom.NodeList) xpe.evaluate(node, XPathConstants.NODESET);
    //         } catch (XPathExpressionException xpee) {
    //             CustomaryContext.create((Context)context).throwConfigurationError(context, xpee, "Could not evaluate XPath '%(xpath)'", "xpath", xpath);
    //             throw (ExceptionConfigurationError) null; // compiler insists
    //         }

    //         for (int n=0; n<result.getLength(); n++) {
    //             result_nodes.add(result.item(n));
    //         }
    //     }

    //     return new YAMLNode(context, result_nodes);
    // }

    // public YAMLNode transform(CallContext context, SourceWithTimestamp transformer_source, Object... parameters) throws TransformationFailure {

    //     Transformer transformer = YAMLUtil.getTransformer(context, transformer_source, this.getOwnerDocument(context).getDocumentURI(), parameters);

    //     Vector<Node> result_nodes = new Vector<Node>();
    //     for (Node node : this.nodes) {
    //         DOMSource source = new DOMSource(node);
    //         DOMResult result = new DOMResult();
    //         YAMLUtil.transform(context, transformer, source, result);
    //         result_nodes.add(result.getNode());
    //     }

    //     return new YAMLNode(context, result_nodes);
    // }

    public java.util.Iterator<YAMLNode> getIterator (CallContext context) {
        return null; // this.getNodes(context).iterator();
    }

    public java.lang.Iterable<YAMLNode> getIterable (CallContext context) {
        return null; // this.getNodes(context);
    }

    // protected Element getSingleElement(CallContext context) {
    //     if (nodes == null || nodes.size() != 1) {
    //         CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot manipulate YAML nodes that do not contain exactly one DOM node");
    //         throw (ExceptionPreConditionViolation) null; // compiler insists
    //     }

    //     Node node = nodes.get(0);

    //     if (node.getNodeType() != Node.ELEMENT_NODE) {
    //         CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot append to YAML nodes that do not contain a DOM Element");
    //         throw (ExceptionPreConditionViolation) null; // compiler insists
    //     }

    //     return (Element) node;
    // }

    // public void appendElement(CallContext context, String element_name, String... attributes) {
    //     Element element = getSingleElement(context);

    //     Element new_element = getOwnerDocument(context).createElement(element_name);
    //     if (attributes != null) {
    //         for (int a=0; a<attributes.length; a+=2) {
    //             new_element.setAttribute(attributes[a], attributes[a+1]);
    //         }
    //     }

    //     element.appendChild(new_element);
    // }

    // public void setText(CallContext context, String text) {
    //     Element element = getSingleElement(context);
    //     Node child;
    //     while ((child = element.getFirstChild()) != null) {
    //         element.removeChild(child);
    //     }
    //     Text new_text = getOwnerDocument(context).createTextNode(text);        

    //     element.appendChild(new_text);
    // }
}
