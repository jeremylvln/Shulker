// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/agones/v1/alpha/sdk.proto

package dev.agones.dev.sdk.alpha;

public final class SdkProto {
  private SdkProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_Empty_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_Empty_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_Count_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_Count_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_Bool_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_Bool_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_PlayerID_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_PlayerID_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_PlayerIDList_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_PlayerIDList_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_Counter_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_Counter_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_GetCounterRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_GetCounterRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_UpdateCounterRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_UpdateCounterRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_List_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_List_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_GetListRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_GetListRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_UpdateListRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_UpdateListRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_AddListValueRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_AddListValueRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_agones_dev_sdk_alpha_RemoveListValueRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_agones_dev_sdk_alpha_RemoveListValueRequest_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\037proto/agones/v1/alpha/sdk.proto\022\024agone" +
      "s.dev.sdk.alpha\032\033google/protobuf/empty.p" +
      "roto\032 google/protobuf/field_mask.proto\"\007" +
      "\n\005Empty\"\035\n\005Count\022\024\n\005count\030\001 \001(\003R\005count\"\032" +
      "\n\004Bool\022\022\n\004bool\030\001 \001(\010R\004bool\"&\n\010PlayerID\022\032" +
      "\n\010playerID\030\001 \001(\tR\010playerID\"\"\n\014PlayerIDLi" +
      "st\022\022\n\004list\030\001 \003(\tR\004list\"O\n\007Counter\022\022\n\004nam" +
      "e\030\001 \001(\tR\004name\022\024\n\005count\030\002 \001(\003R\005count\022\032\n\010c" +
      "apacity\030\003 \001(\003R\010capacity\"\'\n\021GetCounterReq" +
      "uest\022\022\n\004name\030\001 \001(\tR\004name\"\214\001\n\024UpdateCount" +
      "erRequest\0227\n\007counter\030\001 \001(\0132\035.agones.dev." +
      "sdk.alpha.CounterR\007counter\022;\n\013update_mas" +
      "k\030\002 \001(\0132\032.google.protobuf.FieldMaskR\nupd" +
      "ateMask\"N\n\004List\022\022\n\004name\030\001 \001(\tR\004name\022\032\n\010c" +
      "apacity\030\002 \001(\003R\010capacity\022\026\n\006values\030\003 \003(\tR" +
      "\006values\"$\n\016GetListRequest\022\022\n\004name\030\001 \001(\tR" +
      "\004name\"\200\001\n\021UpdateListRequest\022.\n\004list\030\001 \001(" +
      "\0132\032.agones.dev.sdk.alpha.ListR\004list\022;\n\013u" +
      "pdate_mask\030\002 \001(\0132\032.google.protobuf.Field" +
      "MaskR\nupdateMask\"?\n\023AddListValueRequest\022" +
      "\022\n\004name\030\001 \001(\tR\004name\022\024\n\005value\030\002 \001(\tR\005valu" +
      "e\"B\n\026RemoveListValueRequest\022\022\n\004name\030\001 \001(" +
      "\tR\004name\022\024\n\005value\030\002 \001(\tR\005value2\273\010\n\003SDK\022K\n" +
      "\rPlayerConnect\022\036.agones.dev.sdk.alpha.Pl" +
      "ayerID\032\032.agones.dev.sdk.alpha.Bool\022N\n\020Pl" +
      "ayerDisconnect\022\036.agones.dev.sdk.alpha.Pl" +
      "ayerID\032\032.agones.dev.sdk.alpha.Bool\022M\n\021Se" +
      "tPlayerCapacity\022\033.agones.dev.sdk.alpha.C" +
      "ount\032\033.agones.dev.sdk.alpha.Empty\022M\n\021Get" +
      "PlayerCapacity\022\033.agones.dev.sdk.alpha.Em" +
      "pty\032\033.agones.dev.sdk.alpha.Count\022J\n\016GetP" +
      "layerCount\022\033.agones.dev.sdk.alpha.Empty\032" +
      "\033.agones.dev.sdk.alpha.Count\022O\n\021IsPlayer" +
      "Connected\022\036.agones.dev.sdk.alpha.PlayerI" +
      "D\032\032.agones.dev.sdk.alpha.Bool\022V\n\023GetConn" +
      "ectedPlayers\022\033.agones.dev.sdk.alpha.Empt" +
      "y\032\".agones.dev.sdk.alpha.PlayerIDList\022T\n" +
      "\nGetCounter\022\'.agones.dev.sdk.alpha.GetCo" +
      "unterRequest\032\035.agones.dev.sdk.alpha.Coun" +
      "ter\022Z\n\rUpdateCounter\022*.agones.dev.sdk.al" +
      "pha.UpdateCounterRequest\032\035.agones.dev.sd" +
      "k.alpha.Counter\022K\n\007GetList\022$.agones.dev." +
      "sdk.alpha.GetListRequest\032\032.agones.dev.sd" +
      "k.alpha.List\022Q\n\nUpdateList\022\'.agones.dev." +
      "sdk.alpha.UpdateListRequest\032\032.agones.dev" +
      ".sdk.alpha.List\022U\n\014AddListValue\022).agones" +
      ".dev.sdk.alpha.AddListValueRequest\032\032.ago" +
      "nes.dev.sdk.alpha.List\022[\n\017RemoveListValu" +
      "e\022,.agones.dev.sdk.alpha.RemoveListValue" +
      "Request\032\032.agones.dev.sdk.alpha.ListB\230\001\n\030" +
      "dev.agones.dev.sdk.alphaB\010SdkProtoP\001\242\002\004A" +
      "DSA\252\002\024Agones.Dev.Sdk.Alpha\312\002\024Agones\\Dev\\" +
      "Sdk\\Alpha\342\002 Agones\\Dev\\Sdk\\Alpha\\GPBMeta" +
      "data\352\002\027Agones::Dev::Sdk::Alphab\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.EmptyProto.getDescriptor(),
          com.google.protobuf.FieldMaskProto.getDescriptor(),
        });
    internal_static_agones_dev_sdk_alpha_Empty_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_agones_dev_sdk_alpha_Empty_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_Empty_descriptor,
        new java.lang.String[] { });
    internal_static_agones_dev_sdk_alpha_Count_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_agones_dev_sdk_alpha_Count_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_Count_descriptor,
        new java.lang.String[] { "Count", });
    internal_static_agones_dev_sdk_alpha_Bool_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_agones_dev_sdk_alpha_Bool_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_Bool_descriptor,
        new java.lang.String[] { "Bool", });
    internal_static_agones_dev_sdk_alpha_PlayerID_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_agones_dev_sdk_alpha_PlayerID_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_PlayerID_descriptor,
        new java.lang.String[] { "PlayerID", });
    internal_static_agones_dev_sdk_alpha_PlayerIDList_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_agones_dev_sdk_alpha_PlayerIDList_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_PlayerIDList_descriptor,
        new java.lang.String[] { "List", });
    internal_static_agones_dev_sdk_alpha_Counter_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_agones_dev_sdk_alpha_Counter_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_Counter_descriptor,
        new java.lang.String[] { "Name", "Count", "Capacity", });
    internal_static_agones_dev_sdk_alpha_GetCounterRequest_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_agones_dev_sdk_alpha_GetCounterRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_GetCounterRequest_descriptor,
        new java.lang.String[] { "Name", });
    internal_static_agones_dev_sdk_alpha_UpdateCounterRequest_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_agones_dev_sdk_alpha_UpdateCounterRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_UpdateCounterRequest_descriptor,
        new java.lang.String[] { "Counter", "UpdateMask", });
    internal_static_agones_dev_sdk_alpha_List_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_agones_dev_sdk_alpha_List_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_List_descriptor,
        new java.lang.String[] { "Name", "Capacity", "Values", });
    internal_static_agones_dev_sdk_alpha_GetListRequest_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_agones_dev_sdk_alpha_GetListRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_GetListRequest_descriptor,
        new java.lang.String[] { "Name", });
    internal_static_agones_dev_sdk_alpha_UpdateListRequest_descriptor =
      getDescriptor().getMessageTypes().get(10);
    internal_static_agones_dev_sdk_alpha_UpdateListRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_UpdateListRequest_descriptor,
        new java.lang.String[] { "List", "UpdateMask", });
    internal_static_agones_dev_sdk_alpha_AddListValueRequest_descriptor =
      getDescriptor().getMessageTypes().get(11);
    internal_static_agones_dev_sdk_alpha_AddListValueRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_AddListValueRequest_descriptor,
        new java.lang.String[] { "Name", "Value", });
    internal_static_agones_dev_sdk_alpha_RemoveListValueRequest_descriptor =
      getDescriptor().getMessageTypes().get(12);
    internal_static_agones_dev_sdk_alpha_RemoveListValueRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_agones_dev_sdk_alpha_RemoveListValueRequest_descriptor,
        new java.lang.String[] { "Name", "Value", });
    com.google.protobuf.EmptyProto.getDescriptor();
    com.google.protobuf.FieldMaskProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
