package actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import actions.views.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import services.ReportService;

public class TopAction extends ActionBase {

    private ReportService service;

    //indexメソッドを実行する
    @Override
    public void process() throws ServletException, IOException {
        service=new ReportService();

        //メソッドを実行
        invoke();

        service.close();
    }

    //一覧画面を表示する
    public void index() throws ServletException,IOException{
        //セッションからログイン中の従業員情報を取得
        EmployeeView loginEmployee=(EmployeeView)getSessionScope(AttributeConst.LOGIN_EMP);

        //ログイン中の従業員が作成した日報データを、指定されたページ数の一覧画面に表示する分取得
        int page=getPage();
        List<ReportView> reports=service.getMinePerPage(loginEmployee, page);

        //ログイン中の従業員が作成した日報データを取得
        long myReportCount=service.countAllMine(loginEmployee);

        putRequestScope(AttributeConst.REPORTS,reports);
        putRequestScope(AttributeConst.REP_COUNT,myReportCount);
        putRequestScope(AttributeConst.PAGE,page);
        putRequestScope(AttributeConst.MAX_ROW,JpaConst.ROW_PER_PAGE);

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションから削除
        String flush=getSessionScope(AttributeConst.FLUSH);
        if(flush!=null) {
            putRequestScope(AttributeConst.FLUSH,flush);
            removeSessionScope(AttributeConst.FLUSH);
        }
        //一覧画面を表示
        forward(ForwardConst.FW_TOP_INDEX);
    }
}
