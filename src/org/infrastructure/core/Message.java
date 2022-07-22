package org.infrastructure.core;

public class Message {
   private int src;
   private int dst;
   private int typ;
   private Object content;

   public Message(int src, int dst, int typ, Object content) {
      this.src = src;
      this.dst = dst;
      this.typ = typ;
      this.content = content;
   }

   public int getSrc() {
      return this.src;
   }

   public int getDst() {
      return this.dst;
   }

   public int getTyp() {
      return this.typ;
   }

   public Object getContent() {
      return this.content;
   }
}
