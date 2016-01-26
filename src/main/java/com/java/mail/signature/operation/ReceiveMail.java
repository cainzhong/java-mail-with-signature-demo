package com.java.mail.signature.operation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.operator.OperatorCreationException;

import com.java.mail.domain.Attachment;
import com.java.mail.domain.MailMessage;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.WebProxy;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.credential.WebProxyCredentials;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ReceiveMail {

  private static final String PROVIDER_NAME = "BC";
  /* Incoming Mail" server, eg. webmail.hp.com */
  private String host;

  /* eg. SSL ports are 993 for IMAP and 995 for POP3 */
  private String port;

  /* eg. true or false, whether Incoming Mail server needs authentication or not */
  private String auth;

  /* eg. POP3, POP3S, IMAP or IMAPS */
  private String protocol;

  /* eg. joe.smith@hp.com */
  private String username;

  private String password;

  /* eg. true or false, whether Incoming Mail server needs authentication or not */
  private String proxySet;

  private String proxyHost;

  private String proxyPort;

  private String proxyUser;

  private String proxyPassword;

  private String proxyDomain;

  private Session session;

  private Store store;

  private FetchProfile profile;

  public static void main(String args[]) {
    String host = "webmail.hp.com";
    // you must use SSL for incoming mail, SSL ports are 993 for IMAP and 995 for POP3
    String port = "993";
    String auth = "true";
    String protocol = "imaps";
    String username = "tao.zhong@hpe.com";
    String password = "Cisco01!";
    String proxySet = "false";
    String proxyHost = "";
    String proxyPort = "";

    String fromStringTerm = "tao.zhong@hpe.com";
    // String subjectTerm = "Simple Mail without attachment";
    // String subjectTerm = "Simple Mail with attachment";
    // String subjectTerm = "Sign Mail without attachment";
    String subjectTerm = "Sign Mail with attachment";
    String sourceFolderName = "To or Cc me";
    String toFolderName = "Deleted Items";

    Map<String, String> paramMap = new HashMap<String, String>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", "true");
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "Cisco01!");
    paramMap.put("proxySet", "true");
    paramMap.put("proxyHost", "");
    paramMap.put("proxyPort", "");
    JSONArray paramJson = JSONArray.fromObject(paramMap);

    // 将json字符串转换成jsonObject
    JSONObject jsonObject = JSONObject.fromObject(paramJson.toString().substring(1, paramJson.toString().length() - 1));

    Map<String, String> map = ReceiveMail.convertJsonToMap(jsonObject);
    ReceiveMail receiveMail = new ReceiveMail(map.get("host"), map.get("port"), map.get("auth"), map.get("protocol"), map.get("username"), map.get("password"), map.get("proxySet"), map.get("proxyHost"), map.get("proxyPort"), "proxyUser", "proxyPassword", "proxyDomain");
    try {
      receiveMail.receive(fromStringTerm, subjectTerm, sourceFolderName, toFolderName);
      receiveMail.close();
      receiveMail.receiveUsingEWS();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Map<String, String> convertJsonToMap(JSONObject jsonObject) {
    HashMap<String, String> map = new HashMap<String, String>();
    Iterator it = jsonObject.keys();
    while (it.hasNext()) {
      String key = String.valueOf(it.next());
      String value = (String) jsonObject.get(key);
      map.put(key, value);
    }
    return map;
  }

  /**
   * Constructor
   * 
   * @param host
   *            Incoming Mail" server, eg. webmail.hp.com
   * @param port
   *            eg. SSL ports are 993 for IMAP and 995 for POP3
   * @param auth
   *            eg. true or false, whether Incoming Mail server needs authentication or not
   * @param protocol
   *            eg. POP3, POP3S, IMAP or IMAPS
   * @param username
   *            eg. joe.smith@hp.com
   * @param password
   * @param proxySet
   *            true or false, whether Incoming Mail server needs authentication or not
   * @param proxyHost
   * @param proxyPort
   */
  public ReceiveMail(String host, String port, String auth, String protocol, String username, String password, String proxySet, String proxyHost, String proxyPort, String proxyUser, String proxyPassword, String proxyDomain) {
    this.host = host;
    this.port = port;
    this.auth = auth;
    this.protocol = protocol;
    this.username = username;
    this.password = password;
    this.proxySet = proxySet;
    if (proxySet.equalsIgnoreCase("true") && !protocol.equalsIgnoreCase("EWS")) {
      this.proxyHost = proxyHost;
      this.proxyPort = proxyPort;
    }
    // For EWS proxy
    if (proxySet.equalsIgnoreCase("true") && protocol.equalsIgnoreCase("EWS")) {
      this.proxyHost = proxyHost;
      this.proxyPort = proxyPort;
      this.proxyUser = proxyUser;
      this.proxyPassword = proxyPassword;
      this.proxyDomain = proxyDomain;
    }
  }

  /**
   * Receive emails.
   * 
   * @param fromStringTerm
   * @param subjectTerm
   * @param sourceFolderName
   * @param toFolderName
   * @return
   * @throws Exception
   */
  public JSONArray receive(String fromStringTerm, String subjectTerm, String sourceFolderName, String toFolderName) throws Exception {
    // Connect to the mail server
    this.open();

    // open mail folder
    /* POP3Folder can only receive the mails in 'INBOX', IMAPFolder can receive the mails in all folders which created by user. */
    Folder sourceFolder = this.store.getFolder(sourceFolderName);
    sourceFolder.open(Folder.READ_WRITE);

    SearchTerm st = new OrTerm(new FromStringTerm(fromStringTerm), new SubjectTerm(subjectTerm));
    Message[] messages = sourceFolder.search(st);

    System.out.println("Found: " + messages.length + " mails matched with the condition.");

    // Get emails and UID
    sourceFolder.fetch(messages, this.profile);

    List<MailMessage> msgList = this.processMsg(messages, sourceFolder, toFolderName);
    // close the folder, true means that will indeed to delete the message, false means that will not delete the message.
    if (sourceFolder != null && sourceFolder.isOpen()) {
      sourceFolder.close(true);
    }
    JSONArray jsonArray = JSONArray.fromObject(msgList);
    System.out.println("jsonArray: " + jsonArray.toString());
    return jsonArray;
  }

  public void receiveUsingEWS() throws Exception {
    ExchangeService service = new ExchangeService();

    if ("true".equalsIgnoreCase(this.proxySet)) {
      WebProxyCredentials proxyCredentials = new WebProxyCredentials("proxyServerUser", "proxyPassword", "domain");
      WebProxy proxy = new WebProxy("proxyServerHostName", 80, proxyCredentials);
      service.setWebProxy(proxy);
    }

    ExchangeCredentials credentials = new WebCredentials("cainzhong@cainzhong.win", "Cisco01!");
    service.setCredentials(credentials);
    service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
    // service.autodiscoverUrl("cainzhong@cainzhong.win");

    // EmailMessage msg = new EmailMessage(service);
    // msg.setSubject("EWS API!");
    // msg.setBody(MessageBody.getMessageBodyFromText("Sent using the EWS Java API."));
    // msg.getToRecipients().add("cainzhong@cainzhong.win");
    // msg.send();

    microsoft.exchange.webservices.data.core.service.folder.Folder inbox = microsoft.exchange.webservices.data.core.service.folder.Folder.bind(service, WellKnownFolderName.Inbox);
    this.findItems(service);

    System.out.println("end");
  }

  private void findItems(ExchangeService service) throws Exception {
    ItemView view = new ItemView(10);
    view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Ascending);
    view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived));

    FindItemsResults<Item> findResults = service.findItems(WellKnownFolderName.Inbox, new SearchFilter.SearchFilterCollection(LogicalOperator.Or, new SearchFilter.ContainsSubstring(ItemSchema.Subject, "EWS"), new SearchFilter.ContainsSubstring(ItemSchema.Subject, "API")), view);

    System.out.println("Total number of items found: " + findResults.getTotalCount());

    for (Item item : findResults) {
      System.out.println(item.getSubject());
      System.out.println(item.getBody());
      System.out.println(item.getDateTimeReceived());
      // Do something with the item.
    }
  }

  /**
   * Connect to the mail server.
   * 
   * @throws MessagingException
   */
  private void open() throws MessagingException {
    Properties props = this.getProperties();
    this.session = Session.getDefaultInstance(props, null);
    this.store = this.session.getStore(this.protocol);
    this.store.connect(this.host, this.username, this.password);

    this.profile = new FetchProfile();
    this.profile.add(UIDFolder.FetchProfileItem.UID);
    this.profile.add(FetchProfile.Item.ENVELOPE);
  }

  private Properties getProperties() {
    Properties props = new Properties();
    props.put("mail.smtp.host", this.host);
    props.put("mail.smtp.port", this.port);
    props.put("mail.smtp.auth", this.auth);
    props.put("mail.store.protocol", this.protocol);
    // Proxy
    if (this.proxySet.equalsIgnoreCase("true")) {
      props.put("proxySet", this.proxySet);
      props.put("http.proxyHost", this.proxyHost);
      props.put("http.proxyPort", this.proxyPort);
      // props.put("socksProxyHost", proxyHost);
      // props.put("socksProxyPort", proxyPort);
    }
    /*
     * General Issues with Multiparts
     * 
     * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
     */
    // props.put("mail.mime.cachemultipart", false);

    return props;
  }

  private boolean validateSignedMail(Message msg, Folder sourceFolder, String toFolderName) throws MessagingException, CMSException, IOException, OperatorCreationException, CertificateException {
    boolean signaturePassed = false;
    /*
     * Add a header to make a new message in order to fix the issue of Outlook
     * 
     * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
     * 
     * @see http://stackoverflow.com/questions/8590426/s-mime-verification-with-x509-certificate
     */
    MimeMessage newmsg = new MimeMessage((MimeMessage) msg);
    newmsg.setHeader("Nothing", "Add a header for verifying signature only.");
    newmsg.saveChanges();
    SMIMESigned signed = new SMIMESigned((MimeMultipart) newmsg.getContent());
    if (this.isValid(signed)) {
      System.out.println("verification succeeded!");
      signaturePassed = true;
      // this.moveDeleteMessage(msg, sourceFolder, toFolderName);
    } else {
      System.out.println("verification failed!");
      signaturePassed = false;
    }
    return signaturePassed;
  }

  private boolean isValid(CMSSignedData signedData) throws OperatorCreationException, CMSException, CertificateException {
    SignerInformationStore signerStore = signedData.getSignerInfos();
    Iterator<SignerInformation> it = signerStore.getSigners().iterator();
    boolean verify = false;
    while (it.hasNext()) {
      SignerInformation signer = it.next();
      org.bouncycastle.util.Store store = signedData.getCertificates();
      Collection certCollection = store.getMatches(signer.getSID());
      Iterator certIt = certCollection.iterator();
      X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
      X509Certificate certificate = new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate(certHolder);
      verify = signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(PROVIDER_NAME).build(certificate));
    }
    return verify;
  }

  private void moveDeleteMessage(Message msg, Folder sourceFolder, String toFolderName) throws MessagingException {
    Folder toFolder = this.store.getFolder(toFolderName);
    toFolder.open(Folder.READ_ONLY);
    // Move message
    if (null != msg) {
      Message[] needCopyMsgs = new Message[1];
      needCopyMsgs[0] = msg;
      // Copy the msg to the specific folder
      sourceFolder.copyMessages(needCopyMsgs, toFolder);
      // delete the original msg
      // only add a delete flag on the message, it will not indeed to execute the delete operation.
      msg.setFlag(Flags.Flag.DELETED, true);
      System.out.println("Move and delete the message successfully!");
    }
    // close the folder, true means that will indeed to delete the message, false means that will not delete the message.
    if (toFolder != null && toFolder.isOpen()) {
      toFolder.close(true);
    }
  }

  /**
   * @param messages
   * @param sourceFolder
   * @param toFolderName
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws OperatorCreationException
   * @throws CertificateException
   * @throws CMSException
   */
  public List<MailMessage> processMsg(Message[] messages, Folder sourceFolder, String toFolderName) throws MessagingException, IOException, OperatorCreationException, CertificateException, CMSException {
    List<MailMessage> msgList = new ArrayList<MailMessage>();
    MailMessage mailMsg = new MailMessage();

    for (Message msg : messages) {
      System.out.println(msg.getContentType());
      mailMsg.setContentType(msg.getContentType());

      if (msg.isMimeType("text/html") || msg.isMimeType("text/plain")) {
        // simple mail without attachment
        this.setMailMsgForSimpleMail(msg, mailMsg);
      } else if (msg.isMimeType("multipart/mixed")) {
        // simple mail with attachment
        this.setMailMsgForSimpleMail(msg, mailMsg);
      } else if (msg.isMimeType("multipart/signed")) {
        System.out.println("a signed mail");
        boolean signaturePassed = this.validateSignedMail(msg, sourceFolder, toFolderName);
        mailMsg.setHasSignature(true);
        mailMsg.setSignaturePassed(signaturePassed);
        this.setMailMsgForSignedMail(msg, mailMsg);
      } else if (msg.isMimeType("application/pkcs7-mime") || msg.isMimeType("application/x-pkcs7-mime")) {
        System.out.println("a enveloped mail");
      } else {
        System.out.println("not a identified mail");
        throw new CertificateException();
      }
      msgList.add(mailMsg);
    }
    return msgList;
  }

  private MailMessage setMailMsgForSimpleMail(Message msg, MailMessage mailMsg) throws IOException, MessagingException {
    List<Attachment> attachList = new ArrayList<Attachment>();

    mailMsg.setHasSignature(false);
    mailMsg.setSignaturePassed(false);

    Address[] from = msg.getFrom();
    Address[] to = msg.getRecipients(RecipientType.TO);
    Address[] cc = msg.getRecipients(RecipientType.CC);
    Address[] bcc = msg.getRecipients(RecipientType.BCC);
    String subject = msg.getSubject();
    Date sendDate = msg.getSentDate();

    mailMsg.setFrom(from);
    mailMsg.setTo(to);
    mailMsg.setCc(cc);
    mailMsg.setBcc(bcc);
    mailMsg.setSubject(subject);
    mailMsg.setSendDate(sendDate);

    if (msg.getContent() instanceof Multipart) {
      // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachment.
      Multipart multi1 = (Multipart) msg.getContent();
      System.out.println("*********** The email has " + multi1.getCount() + " parts. ***********");
      // process each part in order.
      for (int i = 0, n = multi1.getCount(); i < n; i++) {
        System.out.println("Process Part: " + (i + 1));
        // unpack, get each part of Multipart, part 0 may email text and part 1 may attachment. Or it is another embedded Multipart.
        Part part = multi1.getBodyPart(i);
        if ((part.isMimeType("text/plain") || part.isMimeType("text/html")) && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
          mailMsg.setContent(part.getContent().toString());
        }
        // Process the attachment if it is.
        this.processAttachment(part, mailMsg, attachList);
      }
    } else if (msg.isMimeType("text/plain") || msg.isMimeType("text/html")) {
      mailMsg.setContent(msg.getContent().toString());
    }
    return mailMsg;
  }

  private MailMessage setMailMsgForSignedMail(Message msg, MailMessage mailMsg) throws IOException, MessagingException {
    List<Attachment> attachList = new ArrayList<Attachment>();

    Address[] from = msg.getFrom();
    Address[] to = msg.getRecipients(RecipientType.TO);
    Address[] cc = msg.getRecipients(RecipientType.CC);
    Address[] bcc = msg.getRecipients(RecipientType.BCC);
    String subject = msg.getSubject();
    Date sendDate = msg.getSentDate();

    mailMsg.setFrom(from);
    mailMsg.setTo(to);
    mailMsg.setCc(cc);
    mailMsg.setBcc(bcc);
    mailMsg.setSubject(subject);
    mailMsg.setSendDate(sendDate);

    if (msg.getContent() instanceof Multipart) {
      // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachment.
      Multipart multi1 = (Multipart) msg.getContent();
      System.out.println("*********** The email has " + multi1.getCount() + " parts. ***********");
      // process each part in order.
      for (int i = 0, n = multi1.getCount(); i < n; i++) {
        System.out.println("Process Part" + (i + 1));
        // unpack, get each part of Multipart, part 0 may email text and part 1 may attachment. Or it is another embedded Multipart.
        Part part2 = multi1.getBodyPart(i);
        // determine Part is email text or Multipart.
        if (part2.getContent() instanceof Multipart) {
          Multipart multi2 = (Multipart) part2.getContent();
          System.out.println("*********** Inner Multipart has " + multi2.getCount() + " parts. ***********");
          // process the content in multi2.
          for (int j = 0; j < multi2.getCount(); j++) {
            Part part3 = multi2.getBodyPart(j);
            System.out.println("part3 Content Type:" + part3.getContentType());
            System.out.println("part3 Content:" + part3.getContent());
            // generally if the content type multipart/alternative, it is email text.
            if (part3.isMimeType("multipart/alternative")) {
              if (part3.getContent() instanceof Multipart) {
                Multipart multi3 = (Multipart) part3.getContent();
                System.out.println("*********** Inner Inner Multipart has " + multi3.getCount() + " parts. ***********");
                for (int k = 0; k < multi3.getCount(); k++) {
                  Part part4 = multi3.getBodyPart(k);
                  System.out.println("part4 Content Type:" + part4.getContentType());
                  System.out.println("part4 Content:" + part4.getContent());
                  if ((part4.isMimeType("text/plain") || part4.isMimeType("text/html")) && !Part.ATTACHMENT.equalsIgnoreCase(part4.getDisposition())) {
                    mailMsg.setContent(part4.getContent().toString());
                  }

                }
              }
            }
            if ((part3.isMimeType("text/plain") || part3.isMimeType("text/html")) && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
              mailMsg.setContent(part3.getContent().toString());
            }
            // Process the attachment if it is.
            this.processAttachment(part3, mailMsg, attachList);
          }
        }
        // Process the attachment if it is.
        this.processAttachment(part2, mailMsg, attachList);
      }
    }
    return mailMsg;
  }

  private void processAttachment(Part part, MailMessage mailMsg, List<Attachment> attachList) throws MessagingException, IOException {
    String disposition = part.getDisposition();
    if ((disposition != null) && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
      System.out.println("Attachment File Name: " + part.getFileName());
      System.out.println("Attachment File Type: " + part.getContentType());
      System.out.println("Attachment Content:" + part.getContent());
      java.io.InputStream in = part.getInputStream();
      // 打开附件的输入流
      // 读取附件字节并存储到文件中
      java.io.FileOutputStream out = new FileOutputStream(part.getFileName());
      int data;
      // while ((data = in.read()) != -1) {
      // out.write(data);
      // }
      Attachment attach = new Attachment();
      attach.setFileName(part.getFileName());
      attach.setFileType(part.getContentType());
      attach.setFileStream(out);
      attachList.add(attach);

      mailMsg.setAttachList(attachList);

      in.close();
      out.close();
    }
  }

  /**
   * Close the connection.
   * 
   * @throws MessagingException
   */
  public void close() throws MessagingException {
    this.store.close();
  }
}
