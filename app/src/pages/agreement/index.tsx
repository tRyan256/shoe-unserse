import { ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function AgreementPage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-white">
      <header className="flex items-center px-4 h-12 border-b border-gray-100">
        <button onClick={() => navigate(-1)} className="p-1">
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="flex-1 text-center font-medium pr-6">用户协议</h1>
      </header>

      <div className="p-4 text-sm text-gray-700 leading-relaxed">
        <h2 className="text-base font-bold mb-3">鞋宙用户服务协议</h2>
        
        <p className="mb-4">
          欢迎您使用鞋宙平台服务！为使用鞋宙平台服务（以下简称"本服务"），您应当阅读并遵守《鞋宙用户服务协议》（以下简称"本协议"）。请您务必审慎阅读、充分理解各条款内容，特别是免除或限制责任的相应条款。
        </p>

        <h3 className="font-bold mb-2">一、服务条款的确认和接纳</h3>
        <p className="mb-4">
          1.1 鞋宙平台的各项服务的所有权和运营权归鞋宙所有。<br/>
          1.2 用户在使用鞋宙平台提供的各项服务之前，应仔细阅读本服务协议。<br/>
          1.3 用户一旦注册使用鞋宙平台的服务，即视为用户已了解并完全同意本服务协议各项内容。
        </p>

        <h3 className="font-bold mb-2">二、用户注册</h3>
        <p className="mb-4">
          2.1 用户注册成功后，鞋宙平台将给予每个用户一个用户账号及相应的密码，该用户账号和密码由用户负责保管。<br/>
          2.2 用户对以其用户账号进行的所有活动和事件负法律责任。<br/>
          2.3 用户须对在鞋宙平台的注册信息的真实性、合法性、有效性承担全部责任。
        </p>

        <h3 className="font-bold mb-2">三、使用规则</h3>
        <p className="mb-4">
          3.1 用户在使用鞋宙平台服务过程中，必须遵循以下原则：<br/>
          （1）遵守中国有关的法律和法规；<br/>
          （2）不得为任何非法目的而使用网络服务系统；<br/>
          （3）遵守所有与网络服务有关的网络协议、规定和程序；<br/>
          （4）不得利用鞋宙平台服务进行任何可能对互联网正常运转造成不利影响的行为。
        </p>

        <h3 className="font-bold mb-2">四、商品交易</h3>
        <p className="mb-4">
          4.1 用户在鞋宙平台购买商品时，请仔细确认商品名称、价格、数量、型号规格等信息。<br/>
          4.2 用户下单后，鞋宙平台会根据库存情况确认订单，订单确认后用户需在规定时间内完成支付。<br/>
          4.3 限量款商品采用抽签方式销售，用户参与抽签即表示同意抽签规则。
        </p>

        <h3 className="font-bold mb-2">五、隐私保护</h3>
        <p className="mb-4">
          5.1 鞋宙平台重视对用户隐私的保护，保护隐私是鞋宙平台的一项基本政策。<br/>
          5.2 您提供的登记资料及鞋宙平台保留的有关您的若干其他资料将受到中国有关隐私的法律保护。
        </p>

        <h3 className="font-bold mb-2">六、免责声明</h3>
        <p className="mb-4">
          6.1 用户明确同意其使用鞋宙平台网络服务所存在的风险将完全由其自己承担。<br/>
          6.2 鞋宙平台不担保服务一定能满足用户的要求，也不担保服务不会中断，对服务的及时性、安全性、准确性也都不作担保。
        </p>

        <h3 className="font-bold mb-2">七、协议修改</h3>
        <p className="mb-4">
          7.1 鞋宙平台有权随时修改本协议的任何条款，一旦本协议的内容发生变动，鞋宙平台将会在平台上公布修改之后的协议内容。<br/>
          7.2 如果不同意鞋宙平台对本协议相关条款所做的修改，用户有权停止使用网络服务。
        </p>

        <p className="text-gray-400 text-xs mt-6">
          更新日期：2024年1月1日
        </p>
      </div>
    </div>
  );
}
