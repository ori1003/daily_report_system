package actions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import actions.views.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import services.ReportService;

//日報に関する処理を行うActionクラス
public class ReportAction extends ActionBase {

    private ReportService service;

    //メソッドを実行
    @Override
    public void process() throws ServletException, IOException {
        service=new ReportService();

        //メソッドを実行
        invoke();
        service.close();
    }

    /**
     * 一覧画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException,IOException{
        //指定されたページ数の一覧画面に表示する日報データを取得
        int page=getPage();
        List<ReportView> reports=service.getAllPerpage(page);

        //全日報データの件数を取得
        long reportsCount=service.countAll();

        putRequestScope(AttributeConst.REPORTS,reports);//取得した日報データ
        putRequestScope(AttributeConst.REP_COUNT,reportsCount);//全ての日報データの件数
        putRequestScope(AttributeConst.PAGE,page);//ページ数
        putRequestScope(AttributeConst.MAX_ROW,JpaConst.ROW_PER_PAGE);//1ページに表示するレコード数

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションから削除
        String flush=getSessionScope(AttributeConst.FLUSH);
        if(flush!=null) {
            putRequestScope(AttributeConst.FLUSH,flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //一覧画面を表示
        forward(ForwardConst.FW_REP_INDEX);
    }

    /**
     * 新規登録画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void entryNew() throws ServletException,IOException{
        putRequestScope(AttributeConst.TOKEN,getTokenId());

        //日報情報の空インスタンスに、日報の日付=今日の日付を設定する
        ReportView rv=new ReportView();
        rv.setReportDate(LocalDate.now());
        putRequestScope(AttributeConst.REPORT,rv);//日付のみ設定済みの日報インスタンスをリクエストスコープに設定

        //新規登録画面を表示
        forward(ForwardConst.FW_REP_NEW);
    }

    /**
     * 新規登録を行う
     * @throws ServletException
     * @throws IOException
     */
    public void create() throws ServletException,IOException{
        //CSRF対策用トークンのチェック
        if(checkToken()) {
            //日報の日付が入力されていなければ、今日の日付を設定
            LocalDate day=null;
            if(getRequestParam(AttributeConst.REP_DATE)==null
                    ||getRequestParam(AttributeConst.REP_DATE).equals("")) {
                day=LocalDate.now();
            }else {
                day=LocalDate.parse(getRequestParam(AttributeConst.REP_DATE));
            }

            //セッションからログイン中の従業員情報を取得
            EmployeeView ev=(EmployeeView)getSessionScope(AttributeConst.LOGIN_EMP);

            //パラメータのああ値をもとに日報情報んインスタンスを作成
            ReportView rv=new ReportView(
                    null,
                    ev,//ログインしている従業員を、日報作成者として登録
                    day,
                    getRequestParam(AttributeConst.REP_TITLE),
                    getRequestParam(AttributeConst.REP_CONTENT),
                    null,
                    null);

            //日報情報登録
            List<String> errors=service.creat(rv);

            if(errors.size()>0) {
                //登録中にエラーがあった場合
                putRequestScope(AttributeConst.TOKEN,getTokenId());//CSRF対策トークン
                putRequestScope(AttributeConst.REPORT,rv);//入力された日報データ
                putRequestScope(AttributeConst.ERR,errors);//エラーリスト

                //新規作成画面を再表示
                forward(ForwardConst.FW_REP_NEW);
            }else {
                //登録中にエラーがなかった場合

                //セッションにフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH,MessageConst.I_REGISTERED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP,ForwardConst.CMD_INDEX);
            }
        }
    }

    /**
     * 詳細画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void show() throws ServletException,IOException{
        //idを条件に日報データを取得
        ReportView rv=service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        if(rv==null) {
            //日報データが存在しない場合はエラー
            forward(ForwardConst.FW_ERR_UNKNOWN);
        }else {
            putRequestScope(AttributeConst.REPORT,rv);//取得した日報データ

            forward(ForwardConst.FW_REP_SHOW);
        }
    }
}
