package com.fly.practice.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;

/**
 * 
 * @author kernaling.wong
 *  本类只是演示should对结果的出现是没有影响的,但是影响结果的相关度,可以利用之来进度某些相关的排序搜索
 */
public class LuceneTest {
    private static String INDEX_PATH = "./Index/";
    public static void main(String[] args) {

        if(false){                  //做索引的时候就把 true;
            IndexWriter writer = null;
            try{
                writer = new IndexWriter(INDEX_PATH,new StandardAnalyzer(),MaxFieldLength.LIMITED);
                
                Document doc = new Document();
                Field fieldCode = new Field("Code","1",Field.Store.YES,Field.Index.ANALYZED);
                Field fieldName = new Field("Name","电热炉",Field.Store.YES,Field.Index.ANALYZED);
                Field fieldInfo = new Field("Info","这是一个电热炉",Field.Store.YES,Field.Index.ANALYZED);
                doc.add(fieldCode);
                doc.add(fieldName);
                doc.add(fieldInfo);
                writer.addDocument(doc);
                
                doc = new Document();
                fieldCode = new Field("Code","2",Field.Store.YES,Field.Index.ANALYZED);
                fieldName = new Field("Name","电水壶",Field.Store.YES,Field.Index.ANALYZED);
                fieldInfo = new Field("Info","解放牌电水壶",Field.Store.YES,Field.Index.ANALYZED);
                doc.add(fieldCode);
                doc.add(fieldName);
                doc.add(fieldInfo);
                writer.addDocument(doc);
                
                doc = new Document();
                fieldCode = new Field("Code","3",Field.Store.YES,Field.Index.ANALYZED);
                fieldName = new Field("Name","水杯",Field.Store.YES,Field.Index.ANALYZED);
                fieldInfo = new Field("Info","钢化水杯",Field.Store.YES,Field.Index.ANALYZED);
                doc.add(fieldCode);
                doc.add(fieldName);
                doc.add(fieldInfo);
                writer.addDocument(doc);
                
                doc = new Document();
                fieldCode = new Field("Code","4",Field.Store.YES,Field.Index.ANALYZED);
                fieldName = new Field("Name","碟子",Field.Store.YES,Field.Index.ANALYZED);
                fieldInfo = new Field("Info","质量很好",Field.Store.YES,Field.Index.ANALYZED);
                doc.add(fieldCode);
                doc.add(fieldName);
                doc.add(fieldInfo);
                writer.addDocument(doc);
                
                System.out.println("索引建立完成!");
            }catch(Exception ex){
                ex.printStackTrace();
            }finally{
                if(writer != null){
                    try{
                        writer.close();
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }else{
            
            IndexSearcher search = null;
            IndexReader ir = null;
            try{
                search = new IndexSearcher(INDEX_PATH);
                
                String keyword = "水";       //用户输入了需要搜索的关键字
                
                BooleanQuery bq = new BooleanQuery();
                
                /**
                 * 以下上查询条件,因为无论用户输入的关键字如何都需要全部显示付费会员商品,相关度从高

到低
                 */
                BooleanQuery tmpBQ = new BooleanQuery();            //这是一个
                tmpBQ.add(new TermQuery((new Term("Code","1"))),BooleanClause.Occur.MUST);
                tmpBQ.add(new TermQuery((new Term("Name",keyword))),BooleanClause.Occur.SHOULD);
                tmpBQ.add(new TermQuery((new Term("Info",keyword))),BooleanClause.Occur.SHOULD);
                bq.add(tmpBQ,BooleanClause.Occur.SHOULD);
                
                tmpBQ = new BooleanQuery();
                tmpBQ.add(new TermQuery((new Term("Code","2"))),BooleanClause.Occur.MUST);
                tmpBQ.add(new TermQuery((new Term("Name",keyword))),BooleanClause.Occur.SHOULD);
                tmpBQ.add(new TermQuery((new Term("Info",keyword))),BooleanClause.Occur.SHOULD);
                bq.add(tmpBQ,BooleanClause.Occur.SHOULD);
                
                tmpBQ = new BooleanQuery();
                tmpBQ.add(new TermQuery((new Term("Code","3"))),BooleanClause.Occur.MUST);
                tmpBQ.add(new TermQuery((new Term("Name",keyword))),BooleanClause.Occur.SHOULD);
                tmpBQ.add(new TermQuery((new Term("Info",keyword))),BooleanClause.Occur.SHOULD);
                bq.add(tmpBQ,BooleanClause.Occur.SHOULD);
                
                tmpBQ = new BooleanQuery();
                tmpBQ.add(new TermQuery((new Term("Code","4"))),BooleanClause.Occur.MUST);
                tmpBQ.add(new TermQuery((new Term("Name",keyword))),BooleanClause.Occur.SHOULD);
                tmpBQ.add(new TermQuery((new Term("Info",keyword))),BooleanClause.Occur.SHOULD);
                bq.add(tmpBQ,BooleanClause.Occur.SHOULD);
                
                
                
                Sort sort = new Sort(SortField.FIELD_SCORE);        //按相关度来排序
                TopFieldDocs tdocs = search.search(bq,null,4,sort);
                
                ScoreDoc scoreDocs[] = tdocs.scoreDocs;
                ir = search.getIndexReader();
                for(int i=0;i<scoreDocs.length;i++){
                    Document tmpDoc = ir.document(scoreDocs[i].doc);
                    System.out.println("文档得分:"+scoreDocs[i].score+"\t产品编号:"+tmpDoc.get

("Code")+"\t产品名称:"+tmpDoc.get("Name")+"\t产品说明:"+tmpDoc.get("Info"));
                }
                
            }catch(Exception ex){
                ex.printStackTrace();
            }finally{
                try{
                    if(search != null){
                        search.close();
                    }
                    
                    if(ir != null){
                        ir.close();
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                
            }
        }
        
        
    }
}


