package app;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Stack;

public class Backend {
    private RandomAccessFile file;
    private int[] index;
    public Backend(RandomAccessFile in) throws IOException {
        file=in;
        indexing(0);
    }
    //初始值记录至内存
    public void indexing(int from) throws IOException {
        index = new int[total()];
        for(int i = from ; i < total();i++){
            index[i]=realIndex(i);
        }
    }
    //统计总数
    private int total() throws IOException {
        int i = 0;
        while(realIndex(i)!=-1){
            i++;
        }
        return i;
    }
    //2字节+名字长度+电话长度+名字+电话
    public void add(String aName, String tel) throws IOException {
        file.seek(file.length());
        file.writeUTF(String.format("%02d",aName.length())+String.format("%02d",tel.length())+ aName + tel);
        indexing(0);
    }
    //输入名字，输出相关
    public String search(String aName) throws IOException {
        Stack<Integer> info=matchName(aName);
        String out="";
        while(!info.empty()){
            int a=info.pop();
            out=out+"\n"+readName(a)+" "+readNum(a);
        }
        return out;
    }
    //输入号码，输出相关
    public String searchNum(String aNum) throws IOException {
        Stack<Integer> info=matchNum(aNum);
        String out="";
        while(!info.empty()){
            int a=info.pop();
            out=out+"\n"+readNum(a)+" "+readName(a);
        }
        return out;
    }
    //输入初始值，输出号码
    public String readNum(int realIndex) throws IOException {
        if(realIndex==-1)
            return "------";
        byte[]a = new byte[2];
        file.seek(realIndex+2);
        file.read(a);
        byte[]b = new byte[2];
        file.seek(realIndex+4);
        file.read(b);
        file.seek(realIndex+6+Integer.parseInt(new String(a)));
        byte[] num=new byte[Integer.parseInt(new String(b))];
        file.read(num);
        return new String(num);
    }
    //输入起始值，输出名字
    public String readName(int realIndex) throws IOException {
        if(realIndex==-1)
            return "------";
        file.seek(realIndex+2);
        byte[] i = new byte[2];
        file.read(i);
        file.seek(realIndex+6);
        String a=new String(i);
        byte[] name=new byte[Integer.parseInt(a)];
        file.read(name);
        return new String(name);
    }
    //输入第n组，输出此组起始值
    public int realIndex(int index) throws IOException {
        int seekNum=0;
        for(int i = 0;i < index;i++){
            byte[] a = new byte[2];
            byte[] b = new byte[2];
            seekNum+=2;
            file.seek(seekNum);
            file.read(a);
            seekNum+=2;
            file.seek(seekNum);
            file.read(b);
            String first=new String(a);
            String Sec=new String(b);
            seekNum=seekNum+Integer.parseInt(first)+Integer.parseInt(Sec)+2;
        }
    if(seekNum==file.length())
        return -1;
    return seekNum;
    }
    //输入名字，输出相似初始值
    public Stack<Integer> matchName (String aName) throws IOException {
        Stack<Integer> info=new Stack<>();
        for(int i =0;i<this.index.length;i++){
            boolean flag=true;
            String temp=readName(this.index[i]);
            if(aName.length()<=temp.length()){
                int j =0;
                while(j<aName.length()&&flag){
                    if(aName.charAt(j)!=temp.charAt(j))
                        flag=false;
                    j++;
                }
            }else{flag=false;}
            if(flag)
                info.push(index[i]);
        }
        return info;
    }
    //输入号码，输出相似初始值
    public Stack<Integer> matchNum(String aNum) throws IOException {
        Stack<Integer> info=new Stack<>();
        for(int i = 0 ; i < this.index.length;i++){
            boolean flag=true;
            String temp=readNum(index[i]);
            if(aNum.length()<=temp.length()){
                int j =0;
                while(j<aNum.length()&&flag){
                    if(aNum.charAt(j)!=temp.charAt(j))
                        flag=false;
                    j++;
                }
            }else{flag=false;}
            if(flag)
                info.push(index[i]);
        }
        return info;
    }
    //输入名字，输出第n个
    public int accurateSearch(String aName) throws IOException {
        for(int i =0;i< this.index.length;i++){
            if(aName.equals(readName(this.index[i])))
                return i;
        }
        return -1;
    }
    //输入号码，输出第n个
    public int accurateSearchNum(String aNum) throws IOException {
        for(int i =0;i< this.index.length;i++){
            if(aNum.equals(readNum(this.index[i])))
                return i;
        }
        return -1;
    }
    //输入名字，更改电话
    public void change(String aName,String newNum) throws IOException {
        int index=accurateSearch(aName),next;
        if(index!=-1){
            if(index+1<this.index.length)
                next= this.index[index+1];
            else
                next=-1;
            byte[] temp=new byte[(int)file.length()-next];
            if(next!=-1){
                file.seek(next);
                file.read(temp);
            }
            file.setLength(this.index[index]);
            add(aName, newNum);
            file.seek(file.length());
            if(next!=-1)
                file.write(temp);
            indexing(index-1);
        }else{
            System.out.println("Don't exist!");
        }
    }
    //输入号码，改变人名
    public void changebyNum(String aNum,String newName) throws IOException {
        int index=accurateSearchNum(aNum),next;
        if(index!=-1){
            if(index+1<this.index.length)
                next= this.index[index+1];
            else
                next=-1;
            byte[] temp=new byte[(int)file.length()-next];
            if(next!=-1){
                file.seek(next);
                file.read(temp);
            }
            file.setLength(this.index[index]);
            add(newName, aNum);
            file.seek(file.length());
            if(next!=-1)
                file.write(temp);
            indexing(index-1);
        }else{
            System.out.println("Don't exist!");
        }
    }
    //输入名字，删除
    public void delByName(String aName) throws IOException{
        int index=accurateSearch(aName);
        if(index!=-1){
            int next= this.index[index+1];
            byte[] temp=new byte[(int)file.length()-next];
            if(next!=-1){
                file.seek(next);
                file.read(temp);
            }
            file.setLength(realIndex(index));
            file.seek(file.length());
            if(next!=-1)
                file.write(temp);
            indexing(0);
        }else{
            System.out.println("Don't exist!");
        }
    }
    //调用Python导出xls
    public void export(String aName) throws IOException, InterruptedException {
        String args="";
        for(int i =0;i<this.index.length;i++){
            args=args+"\t"+readName(index[i])+"\t"+readNum(index[i]);
        }
        Process proc = Runtime.getRuntime().exec("Python3 src/app/Excel.py "+args+"\t"+aName);                        
        proc.waitFor();
    }
}